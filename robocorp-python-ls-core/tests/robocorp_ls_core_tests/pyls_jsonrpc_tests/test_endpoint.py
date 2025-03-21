# Original work Copyright 2018 Palantir Technologies, Inc. (MIT)
# See ThirdPartyNotices.txt in the project root for license information.
# All modifications Copyright (c) Robocorp Technologies Inc.
# All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License")
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http: // www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import time

import mock
import pytest

from robocorp_ls_core.jsonrpc import exceptions
from robocorp_ls_core.jsonrpc.endpoint import Endpoint, require_monitor


# pylint: disable=redefined-outer-name
from concurrent import futures
import io
from robocorp_ls_core.protocols import IMonitor


MSG_ID = "id"


@pytest.fixture()
def dispatcher():
    return {}


@pytest.fixture()
def consumer():
    return mock.MagicMock()


@pytest.fixture()
def endpoint(dispatcher, consumer):
    return Endpoint(dispatcher, consumer, id_generator=lambda: MSG_ID)


def test_bad_message(endpoint):
    # Ensure doesn't raise for a bad message
    endpoint.consume({"key": "value"})


def test_notify(endpoint, consumer):
    endpoint.notify("methodName", {"key": "value"})
    consumer.assert_called_once_with(
        {"jsonrpc": "2.0", "method": "methodName", "params": {"key": "value"}}
    )


def test_notify_none_params(endpoint, consumer):
    endpoint.notify("methodName", None)
    consumer.assert_called_once_with({"jsonrpc": "2.0", "method": "methodName"})


def test_request(endpoint, consumer):
    future = endpoint.request("methodName", {"key": "value"})
    assert not future.done()

    consumer.assert_called_once_with(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    # Send the response back to the endpoint
    result = 1234
    endpoint.consume({"jsonrpc": "2.0", "id": MSG_ID, "result": result})

    assert future.result(timeout=2) == result


def test_request_error(endpoint, consumer):
    future = endpoint.request("methodName", {"key": "value"})
    assert not future.done()

    consumer.assert_called_once_with(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    # Send an error back from the client
    error = exceptions.JsonRpcInvalidRequest(data=1234)
    endpoint.consume({"jsonrpc": "2.0", "id": MSG_ID, "error": error.to_dict()})

    # Verify the exception raised by the future is the same as the error the client serialized
    with pytest.raises(exceptions.JsonRpcException) as exc_info:
        assert future.result(timeout=2)
    assert exc_info.type == exceptions.JsonRpcInvalidRequest
    assert exc_info.value == error


def test_request_cancel(endpoint, consumer):
    from robocorp_ls_core.jsonrpc.exceptions import JsonRpcRequestCancelled

    future = endpoint.request("methodName", {"key": "value"})
    assert not future.done()

    consumer.assert_called_once_with(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    # Cancel the request
    future.set_exception(JsonRpcRequestCancelled())
    consumer.assert_any_call(
        {"jsonrpc": "2.0", "method": "$/cancelRequest", "params": {"id": MSG_ID}}
    )

    with pytest.raises(exceptions.JsonRpcException) as exc_info:
        assert future.result(timeout=2)
    assert exc_info.type == exceptions.JsonRpcRequestCancelled


def test_consume_notification(endpoint, dispatcher):
    handler = mock.Mock()
    dispatcher["methodName"] = handler

    endpoint.consume(
        {"jsonrpc": "2.0", "method": "methodName", "params": {"key": "value"}}
    )
    handler.assert_called_once_with({"key": "value"})


def test_consume_notification_error(endpoint, dispatcher):
    handler = mock.Mock(side_effect=ValueError)
    dispatcher["methodName"] = handler
    # Verify the consume doesn't throw
    endpoint.consume(
        {"jsonrpc": "2.0", "method": "methodName", "params": {"key": "value"}}
    )
    handler.assert_called_once_with({"key": "value"})


def test_consume_notification_method_not_found(endpoint):
    # Verify consume doesn't throw for method not found
    endpoint.consume(
        {"jsonrpc": "2.0", "method": "methodName", "params": {"key": "value"}}
    )


def test_consume_async_notification_error(endpoint, dispatcher):
    def _async_handler():
        raise ValueError()

    handler = mock.Mock(return_value=_async_handler)
    dispatcher["methodName"] = handler

    # Verify the consume doesn't throw
    endpoint.consume(
        {"jsonrpc": "2.0", "method": "methodName", "params": {"key": "value"}}
    )
    handler.assert_called_once_with({"key": "value"})


def test_consume_request(endpoint, consumer, dispatcher):
    result = 1234
    handler = mock.Mock(return_value=result)
    dispatcher["methodName"] = handler

    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    handler.assert_called_once_with({"key": "value"})
    consumer.assert_called_once_with({"jsonrpc": "2.0", "id": MSG_ID, "result": result})


def test_consume_future_request(endpoint, consumer, dispatcher):
    future_response = futures.ThreadPoolExecutor().submit(lambda: 1234)
    handler = mock.Mock(return_value=future_response)
    dispatcher["methodName"] = handler

    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    handler.assert_called_once_with({"key": "value"})
    await_assertion(
        lambda: consumer.assert_called_once_with(
            {"jsonrpc": "2.0", "id": MSG_ID, "result": 1234}
        )
    )


def test_consume_async_request(endpoint, consumer, dispatcher):
    def _async_handler():
        return 1234

    handler = mock.Mock(return_value=_async_handler)
    dispatcher["methodName"] = handler

    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    handler.assert_called_once_with({"key": "value"})
    await_assertion(
        lambda: consumer.assert_called_once_with(
            {"jsonrpc": "2.0", "id": MSG_ID, "result": 1234}
        )
    )


@pytest.mark.parametrize(
    "exc_type, error",
    [
        (ValueError, exceptions.JsonRpcInternalError(message="ValueError")),
        (KeyError, exceptions.JsonRpcInternalError(message="KeyError")),
        (exceptions.JsonRpcMethodNotFound, exceptions.JsonRpcMethodNotFound()),
    ],
)
def test_consume_async_request_error(exc_type, error, endpoint, consumer, dispatcher):
    def _async_handler():
        raise exc_type()

    handler = mock.Mock(return_value=_async_handler)
    dispatcher["methodName"] = handler

    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    handler.assert_called_once_with({"key": "value"})
    await_assertion(lambda: assert_consumer_error(consumer, error))


def test_consume_request_method_not_found(endpoint, consumer):
    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )
    assert_consumer_error(consumer, exceptions.JsonRpcMethodNotFound.of("methodName"))


@pytest.mark.parametrize(
    "exc_type, error",
    [
        (ValueError, exceptions.JsonRpcInternalError(message="ValueError")),
        (KeyError, exceptions.JsonRpcInternalError(message="KeyError")),
        (exceptions.JsonRpcMethodNotFound, exceptions.JsonRpcMethodNotFound()),
    ],
)
def test_consume_request_error(exc_type, error, endpoint, consumer, dispatcher):
    handler = mock.Mock(side_effect=exc_type)
    dispatcher["methodName"] = handler

    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )

    handler.assert_called_once_with({"key": "value"})
    await_assertion(lambda: assert_consumer_error(consumer, error))


def test_consume_request_cancel(endpoint, dispatcher, consumer):
    def async_handler():
        time.sleep(1)
        return 1234

    handler = mock.Mock(return_value=async_handler)
    dispatcher["methodName"] = handler

    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )
    handler.assert_called_once_with({"key": "value"})

    endpoint.consume(
        {"jsonrpc": "2.0", "method": "$/cancelRequest", "params": {"id": MSG_ID}}
    )

    # Because Python's Future cannot be cancelled once it's started, the request
    # may not actually be cancelled (to have a monitor that can be cancelled
    # afterwards, it's possible to set __require_monitor__ in the callable. see:
    # test_consume_request_cancel_monitor).
    def check_result_or_cancelled():
        try:
            consumer.assert_called_once_with(
                {"jsonrpc": "2.0", "id": MSG_ID, "result": 1234}
            )
        except AssertionError:
            consumer.assert_called_once_with(
                {
                    "jsonrpc": "2.0",
                    "id": MSG_ID,
                    "error": exceptions.JsonRpcRequestCancelled().to_dict(),
                }
            )

    await_assertion(check_result_or_cancelled, timeout=5)


def test_log_slow_calls(endpoint, dispatcher, consumer, monkeypatch):
    from robocorp_ls_core.robotframework_log import configure_logger

    monkeypatch.setattr(endpoint, "SHOW_THREAD_DUMP_AFTER_TIMEOUT", 0.5)

    def async_slow_handler():
        time.sleep(1)
        return 1234

    s = io.StringIO()
    log_level = 2
    with configure_logger("", log_level, s):

        handler = mock.Mock(return_value=async_slow_handler)
        dispatcher["methodName"] = handler

        endpoint.consume(
            {
                "jsonrpc": "2.0",
                "id": MSG_ID,
                "method": "methodName",
                "params": {"key": "value"},
            }
        )
        handler.assert_called_once_with({"key": "value"})

        def check_result():
            consumer.assert_called_once_with(
                {"jsonrpc": "2.0", "id": MSG_ID, "result": 1234}
            )

        await_assertion(check_result, timeout=3)

    assert "time.sleep(1)" in s.getvalue()
    assert "in async_slow_handler" in s.getvalue()


def test_consume_request_cancel_monitor(endpoint, dispatcher, consumer, monkeypatch):

    from robocorp_ls_core.jsonrpc import endpoint as endpoint_module

    monkeypatch.setattr(endpoint_module, "FORCE_NON_THREADED_VERSION", False)

    endpoint_module.FORCE_NON_THREADED_VERSION = False
    # i.e.: cancel after the request already started.
    @require_monitor
    def async_handler(monitor: IMonitor):
        for _ in range(10):
            time.sleep(0.1)
            monitor.check_cancelled()

    handler = mock.Mock(return_value=async_handler)
    dispatcher["methodName"] = handler

    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "id": MSG_ID,
            "method": "methodName",
            "params": {"key": "value"},
        }
    )
    handler.assert_called_once_with({"key": "value"})

    endpoint.consume(
        {"jsonrpc": "2.0", "method": "$/cancelRequest", "params": {"id": MSG_ID}}
    )

    def wait_for_monitor_check_cancelled():
        consumer.assert_called_once_with(
            {
                "jsonrpc": "2.0",
                "id": MSG_ID,
                "error": exceptions.JsonRpcRequestCancelled().to_dict(),
            }
        )

    await_assertion(wait_for_monitor_check_cancelled)


def test_consume_request_cancel_unknown(endpoint):
    # Verify consume doesn't throw
    endpoint.consume(
        {
            "jsonrpc": "2.0",
            "method": "$/cancelRequest",
            "params": {"id": "unknown identifier"},
        }
    )


def assert_consumer_error(consumer_mock, exception):
    """Assert that the consumer mock has had once call with the given error message and code.

    The error's data part is not compared since it contains the traceback.
    """
    assert len(consumer_mock.mock_calls) == 1
    _name, args, _kwargs = consumer_mock.mock_calls[0]
    assert args[0]["error"]["message"] == exception.message
    assert args[0]["error"]["code"] == exception.code


def await_assertion(condition, timeout=3.0, interval=0.1):
    maxtime = time.time() + timeout
    while True:
        try:
            condition()
        except AssertionError as e:
            if time.time() <= maxtime:
                time.sleep(interval)
            else:
                raise e
        else:
            return

# Copyright (c) Robocorp Technologies Inc.
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


def test_invalid_launch_1(debugger_api):
    """
    :param _DebuggerAPI debugger_api:
    """
    from robotframework_debug_adapter.dap.dap_schema import LaunchRequest
    from robotframework_debug_adapter.dap.dap_schema import LaunchRequestArguments
    from robotframework_debug_adapter.dap.dap_schema import LaunchResponse

    debugger_api.initialize()

    debugger_api.write(
        LaunchRequest(
            LaunchRequestArguments(
                __sessionId="some_id",
                noDebug=True,
                # target=target, -- error: don't add target
                terminal="none",
                cwd=None,
            )
        )
    )

    launch_response = debugger_api.read(LaunchResponse)
    assert launch_response.success == False


def test_invalid_launch_2(debugger_api):
    """
    :param _DebuggerAPI debugger_api:
    """

    debugger_api.initialize()

    debugger_api.launch("invalid_file.robot", debug=False, success=False)


def test_simple_launch(debugger_api):
    """
    This is an integrated test of the debug adapter. It communicates with it as if it was
    VSCode.
    
    :param _DebuggerAPI debugger_api:
    """
    from robotframework_debug_adapter.dap.dap_schema import TerminatedEvent
    from robotframework_debug_adapter.dap.dap_schema import OutputEvent

    debugger_api.initialize()

    target = debugger_api.get_dap_case_file("case_log.robot")
    debugger_api.launch(target, debug=False)
    debugger_api.configuration_done()

    debugger_api.read(TerminatedEvent)
    debugger_api.assert_message_found(
        OutputEvent, lambda msg: "check that log works" in msg.body.output
    )


def test_simple_debug_launch(debugger_api):
    """
    :param _DebuggerAPI debugger_api:
    """
    from robotframework_debug_adapter.dap.dap_schema import TerminatedEvent

    debugger_api.initialize()
    target = debugger_api.get_dap_case_file("case_log.robot")

    debugger_api.launch(target, debug=True)
    debugger_api.set_breakpoints(target, 4)
    debugger_api.configuration_done()

    json_hit = debugger_api.wait_for_thread_stopped()

    debugger_api.continue_event()

    debugger_api.read(TerminatedEvent)


def test_step_in(debugger_api):
    from robotframework_debug_adapter.dap.dap_schema import TerminatedEvent

    debugger_api.initialize()
    target = debugger_api.get_dap_case_file("case4/case4.robot")
    debugger_api.target = target

    debugger_api.launch(target, debug=True)
    debugger_api.set_breakpoints(
        target, debugger_api.get_line_index_with_content("My Equal Redefined   2   2")
    )
    debugger_api.configuration_done()

    json_hit = debugger_api.wait_for_thread_stopped(name="My Equal Redefined")

    debugger_api.step_in(json_hit.thread_id)

    json_hit = debugger_api.wait_for_thread_stopped("step", name="Should Be Equal")

    debugger_api.continue_event()

    debugger_api.read(TerminatedEvent)


def test_step_next(debugger_api):
    from robotframework_debug_adapter.dap.dap_schema import TerminatedEvent

    debugger_api.initialize()
    target = debugger_api.get_dap_case_file("case4/case4.robot")
    debugger_api.target = target

    debugger_api.launch(target, debug=True)
    debugger_api.set_breakpoints(
        target, debugger_api.get_line_index_with_content("My Equal Redefined   2   2")
    )
    debugger_api.configuration_done()

    json_hit = debugger_api.wait_for_thread_stopped(name="My Equal Redefined")

    debugger_api.step_next(json_hit.thread_id)

    json_hit = debugger_api.wait_for_thread_stopped(
        "step", name="Yet Another Equal Redefined"
    )

    debugger_api.continue_event()

    debugger_api.read(TerminatedEvent)


def test_launch_in_external_terminal(debugger_api):
    """
    This is an integrated test of the debug adapter. It communicates with it as if it was
    VSCode.
    
    :param _DebuggerAPI debugger_api:
    """
    from robotframework_debug_adapter.dap.dap_schema import TerminatedEvent

    debugger_api.initialize()

    target = debugger_api.get_dap_case_file("case_log.robot")
    debugger_api.launch(target, debug=False, terminal="external")
    debugger_api.configuration_done()
    debugger_api.read(TerminatedEvent)

def check_symbol(symbols, name):
    for symbol in symbols:
        if symbol["name"] == name:
            return

    raise AssertionError(f"Did not find: {name}")


def test_workspace_symbols(workspace, libspec_manager):
    from robotframework_ls.impl.workspace_symbols import workspace_symbols
    from robotframework_ls.impl.completion_context import BaseContext
    from robocorp_ls_core.basic import wait_for_condition
    from robocorp_ls_core.constants import NULL
    from robocorp_ls_core.config import Config

    workspace.set_root("case4", libspec_manager=libspec_manager, index_workspace=True)

    config = Config()

    def find_expected_symbols():
        symbols = workspace_symbols("", BaseContext(workspace.ws, config, NULL))
        assert len(symbols) > 0

        try:
            check_symbol(symbols, "List Files In Directory")
            check_symbol(symbols, "Yet Another Equal Redefined")
        except AssertionError:
            return False
        return True

    wait_for_condition(find_expected_symbols)

    symbols = workspace_symbols("", BaseContext(workspace.ws, config, NULL))
    symbols2 = workspace_symbols("", BaseContext(workspace.ws, config, NULL))
    assert symbols == symbols2

    symbols3 = workspace_symbols(
        "Yet Another Equal", BaseContext(workspace.ws, config, NULL)
    )

    # i.e.: Expect the client to filter it afterwards.
    assert symbols == symbols3


def test_workspace_symbols_same_basename(workspace, libspec_manager):
    from robotframework_ls.impl.workspace_symbols import workspace_symbols
    from robotframework_ls.impl.completion_context import BaseContext
    from robocorp_ls_core.constants import NULL
    from robocorp_ls_core.config import Config

    workspace.set_root("case_same_basename", libspec_manager=libspec_manager)
    # Needed to pre-generate the information
    libspec_manager.get_library_doc_or_error(
        libname="my_library",
        create=True,
        current_doc_uri=workspace.get_doc("tasks1.robot").uri,
    )
    libspec_manager.get_library_doc_or_error(
        libname="my_library",
        create=True,
        current_doc_uri=workspace.get_doc("directory/tasks2.robot").uri,
    )

    config = Config()

    symbols = workspace_symbols("", BaseContext(workspace.ws, config, NULL))
    assert len(symbols) > 0

    check_symbol(symbols, "In Lib 1")
    check_symbol(symbols, "In Lib 2")

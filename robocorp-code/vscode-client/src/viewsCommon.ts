import * as vscode from "vscode";
import { TREE_VIEW_ROBOCORP_LOCATORS_TREE, TREE_VIEW_ROBOCORP_ROBOTS_TREE } from "./robocorpViews";

/**
 * Note: if type is error|info the name is the message to be shown.
 */
export interface LocatorEntry {
    name: string;
    line: number;
    column: number;
    type: string; // "browser", "image", "coordinate", "error", "info",...
    filePath: string;
}

export enum RobotEntryType {
    Robot,
    Task,
    Error,
}

export interface CloudEntry {
    label: string;
    iconPath?: string;
    command?: vscode.Command;
    children?: CloudEntry[];
    viewItemContextValue?: string;
}

export interface RobotEntry {
    label: string;
    uri: vscode.Uri | undefined;
    robot: LocalRobotMetadataInfo | undefined;
    taskName?: string;
    iconPath: string;
    type: RobotEntryType;
    parent: RobotEntry | undefined;
}

export interface FSEntry {
    name: string;
    isDirectory: boolean;
    filePath: string;
}

export let treeViewIdToTreeView: Map<string, vscode.TreeView<any>> = new Map();
export let treeViewIdToTreeDataProvider: Map<string, vscode.TreeDataProvider<any>> = new Map();

export function refreshTreeView(treeViewId: string) {
    let dataProvider: any = <any>treeViewIdToTreeDataProvider.get(treeViewId);
    if (dataProvider) {
        dataProvider.fireRootChange();
    }
}

export interface SingleTreeSelectionOpts {
    noSelectionMessage?: string;
    moreThanOneSelectionMessage?: string;
}

async function getSingleTreeSelection<T>(treeId: string, opts?: any): Promise<T | undefined> {
    const noSelectionMessage: string | undefined = opts?.noSelectionMessage;
    const moreThanOneSelectionMessage: string | undefined = opts?.moreThanOneSelectionMessage;

    const robotsTree = treeViewIdToTreeView.get(treeId);
    if (!robotsTree || robotsTree.selection.length == 0) {
        if (noSelectionMessage) {
            vscode.window.showWarningMessage(noSelectionMessage);
        }
        return undefined;
    }

    if (robotsTree.selection.length > 1) {
        if (moreThanOneSelectionMessage) {
            vscode.window.showWarningMessage(moreThanOneSelectionMessage);
        }
        return undefined;
    }

    let element = robotsTree.selection[0];
    return element;
}

let _onSelectedRobotChanged: vscode.EventEmitter<RobotEntry> = new vscode.EventEmitter<RobotEntry>();
export let onSelectedRobotChanged: vscode.Event<RobotEntry> = _onSelectedRobotChanged.event;

let lastSelectedRobot: RobotEntry | undefined = undefined;
export function setSelectedRobot(robotEntry: RobotEntry | undefined) {
    lastSelectedRobot = robotEntry;
    _onSelectedRobotChanged.fire(robotEntry);
}

/**
 * Returns the selected robot or undefined if there are no robots or if more than one robot is selected.
 *
 * If the messages are passed as a parameter, a warning is shown with that message if the selection is invalid.
 */
export function getSelectedRobot(opts?: SingleTreeSelectionOpts): RobotEntry | undefined {
    let ret = lastSelectedRobot;
    if (!ret) {
        if (opts?.noSelectionMessage) {
            vscode.window.showWarningMessage(opts.noSelectionMessage);
        }
    }
    return ret;
}

export async function getSelectedLocator(opts?: SingleTreeSelectionOpts): Promise<LocatorEntry | undefined> {
    return await getSingleTreeSelection<LocatorEntry | undefined>(TREE_VIEW_ROBOCORP_LOCATORS_TREE, opts);
}

export function basename(s) {
    return s.split("\\").pop().split("/").pop();
}

export const debounce = (func, wait) => {
    let timeout;

    return function wrapper(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };

        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
};

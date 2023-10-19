import * as vscode from "vscode";
import {minimatch} from "minimatch";
import _defaultAssociations from "./associations.json";
import {toQuickPickItem, combine, toMap} from "./utils";
import {DocumentationAssociation} from "./types";

const defaultAssociations = _defaultAssociations as DocumentationAssociation[];
let associations = new Map();

function updateAssociations() {
    const config = vscode.workspace.getConfiguration("dayz-ce-schema");
    const userAssociations = config.get("documentationAssociations") as DocumentationAssociation[];
    associations = combine(toMap(defaultAssociations), toMap(userAssociations));
}

updateAssociations();

vscode.workspace.onDidChangeConfiguration(e => {
    if (e.affectsConfiguration("dayz-ce-schema.documentationAssociations")) {
        updateAssociations();
    }
});

export const documentationCommand = "dayz-ce-schema.showDocumentation";

export async function documentationHandler() {
    const activeFileName = vscode.window.activeTextEditor?.document?.fileName;
    if (!activeFileName) {
        return;
    }

    const matches = [];
    for (let [pattern, links] of associations) {
        if (minimatch(activeFileName, pattern, {nocase: true})) {
            matches.push(...links);
        }
    }

    if (!matches || matches.length == 0) {
        vscode.window.showInformationMessage("No documentation available for this file");
        return;
    }

    if (matches.length == 1) {
        vscode.env.openExternal(vscode.Uri.parse(matches[0].url));
        return;
    } else {
        const items = matches.map(toQuickPickItem);
        const item = await vscode.window.showQuickPick(items, {placeHolder: "Pick page to open"});

        if (item) {
            vscode.env.openExternal(vscode.Uri.parse(item.url));
        }
    }
}

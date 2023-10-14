import * as vscode from "vscode";
import { minimatch } from "minimatch";
import defaultAssociations from "./associations.json";

type DocumentationLink = { title: string, url: string };

function toQuickPickItem(link: DocumentationLink) {
    return {
        label: link.title,
        detail: link.url,
        url: link.url
    }
}

export const documentationCommand = "dayz-ce-schema.showDocumentation";

export async function documentationHandler() {
    const activeFileName = vscode.window.activeTextEditor?.document?.fileName;
    if (!activeFileName) {
        return;
    }

    let matches = defaultAssociations
        .filter(a => minimatch(activeFileName, a.pattern, { nocase: true }))
        .flatMap(a => a.links);

    if (!matches) {
        vscode.window.showInformationMessage("No documentation available for this file");
        return;
    }

    if (matches.length == 1) {
        vscode.env.openExternal(vscode.Uri.parse(matches[0].url));
        return;
    }
    else {
        const items = matches.map(toQuickPickItem);
        const item = await vscode.window.showQuickPick(items, {placeHolder: "Pick page to open"});
        
        if (item) {
            vscode.env.openExternal(vscode.Uri.parse(item.url));
        }
    }
}
import fs from "fs";
import {QuickPickItemKind, Range, Uri, commands, window, workspace} from "vscode";
import {TextDocumentIdentifier, WorkspaceEdit} from "vscode-languageclient";
import {createConverter as createP2CConverter} from "vscode-languageclient/lib/common/protocolConverter";

const p2cConverter = createP2CConverter(undefined, true, true);

export const applyCustomFilesRefactorCommand = "dayz-ce-schema.applyCustomFilesRefactor";

export async function applyCustomFilesRefactorHandler(
    kind: string,
    range: Range,
    options: string[],
    dayzFileType: number
) {
    const documentItems: {label: string; description: string; uri: Uri | null}[] = options
        .map(o => Uri.parse(o))
        .map(o => ({label: workspace.asRelativePath(o, false), description: o.fsPath.toString(), uri: o}));

    if (documentItems.length > 0) {
        //@ts-ignore
        documentItems.unshift({label: "Existing files", kind: QuickPickItemKind.Separator});
        //@ts-ignore
        documentItems.push({label: "New", kind: QuickPickItemKind.Separator});
    }
    documentItems.push({label: "New File", description: "Create a new file", uri: null});

    const selectedDocumentItem = await window.showQuickPick(documentItems, {
        placeHolder: "Pick destination file",
        canPickMany: false
    });

    if (!selectedDocumentItem) {
        return;
    }

    let targetUri: Uri;
    if (selectedDocumentItem.uri == null) {
        const newFileName = await window.showInputBox({prompt: "Enter the name of the new file"});
        if (!newFileName) {
            return;
        }

        targetUri = Uri.joinPath(workspace.workspaceFolders![0].uri, newFileName);

        if (fs.existsSync(targetUri.fsPath)) {
            await window.showErrorMessage(`The file "${workspace.asRelativePath(targetUri, false)}" already exists.`);
            return;
        }

        const newFileLsEdit = await commands.executeCommand(
            "xml.workspace.executeCommand",
            "dayz-ce-schema.createNewFile",
            targetUri.toString(),
            dayzFileType
        );

        if (!newFileLsEdit) {
            await window.showErrorMessage("Error occurred during new file creation");
            return;
        }

        const edit = await p2cConverter.asWorkspaceEdit(newFileLsEdit);
        await workspace.applyEdit(edit);
        const newDoc = workspace.textDocuments.find(doc => doc.uri.toString() === targetUri.toString());
        if (newDoc) {
            await newDoc.save();
        }
    } else {
        targetUri = selectedDocumentItem.uri;
    }

    const sourceDocument = window.activeTextEditor?.document;

    const identifier = TextDocumentIdentifier.create(sourceDocument!.uri.toString());

    const lsEdit: WorkspaceEdit | null = await commands.executeCommand(
        "xml.workspace.executeCommand",
        "dayz-ce-schema.computeRefactorEdit",
        identifier,
        targetUri.toString(),
        kind,
        range
    );

    if (!lsEdit) {
        await window.showErrorMessage("Error occurred during refactoring");
        return;
    }

    const edit = await p2cConverter.asWorkspaceEdit(lsEdit);

    await workspace.applyEdit(edit);
    await commands.executeCommand("vscode.open", targetUri);
}

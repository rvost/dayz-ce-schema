import {Range, Uri, commands, window, workspace} from "vscode";
import {TextDocumentIdentifier, WorkspaceEdit} from "vscode-languageclient";
import {createConverter as createP2CConverter} from "vscode-languageclient/lib/common/protocolConverter";

const p2cConverter = createP2CConverter(undefined, true, true);

export const applyCustomFilesRefactorCommand = "dayz-ce-schema.applyCustomFilesRefactor";

export async function applyCustomFilesRefactorHandler(kind: string, range: Range, options: string[]) {
    const documentItems = options
        .map(o => Uri.parse(o))
        .map(o => ({label: workspace.asRelativePath(o, false), description: o.toString(), uri: o}));

    const selectDocumentItem = await window.showQuickPick(documentItems, {
        placeHolder: "Pick page to open",
        canPickMany: false
    });

    if (!selectDocumentItem) {
        return;
    }

    const sourceDocument = window.activeTextEditor?.document;
    const targetUri = selectDocumentItem.uri;

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

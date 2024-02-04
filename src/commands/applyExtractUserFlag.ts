import {Range, window, commands, workspace} from "vscode";
import {TextDocumentIdentifier, WorkspaceEdit} from "vscode-languageclient";
import {createConverter as createP2CConverter} from "vscode-languageclient/lib/common/protocolConverter";

const p2cConverter = createP2CConverter(undefined, true, true);

export const applyExtractUserFlagCommand = "dayz-ce-schema.applyExtractUserFlag";

export async function applyExtractUserFlagHandler(flagType: string, flags: string[], selectedRange: Range) {
    const flagName = await window.showInputBox({prompt: "Enter a name for the new user flag"});
    if (!flagName) {
        return;
    }

    const sourceDocument = window.activeTextEditor?.document;
    const identifier = TextDocumentIdentifier.create(sourceDocument!.uri.toString());
    const lsEdit: WorkspaceEdit | null = await commands.executeCommand(
        "xml.workspace.executeCommand",
        "dayz-ce-schema.computeExtractUserFlag",
        identifier,
        flagType,
        flagName,
        flags,
        selectedRange
    );

    if (!lsEdit) {
        await window.showErrorMessage("Error occurred during refactoring");
        return;
    }

    const edit = await p2cConverter.asWorkspaceEdit(lsEdit);

    await workspace.applyEdit(edit);
}

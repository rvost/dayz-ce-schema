import {Range, window, commands, workspace} from "vscode";
import {TextDocumentIdentifier, WorkspaceEdit} from "vscode-languageclient";
import {createConverter as createP2CConverter} from "vscode-languageclient/lib/common/protocolConverter";

const p2cConverter = createP2CConverter(undefined, true, true);

export const applyExtractRandomPresetCommand = "dayz-ce-schema.applyExtractRandomPreset";

export async function applyExtractRandomPresetHandler(range: Range) {
    const presetName = await window.showInputBox({prompt: "Enter a name for the new preset"});
    if (!presetName) {
        return;
    }

    const sourceDocument = window.activeTextEditor?.document;
    const identifier = TextDocumentIdentifier.create(sourceDocument!.uri.toString());
    const lsEdit: WorkspaceEdit | null = await commands.executeCommand(
        "xml.workspace.executeCommand",
        "dayz-ce-schema.computeExtractRandomPreset",
        identifier,
        range,
        presetName
    );

    if (!lsEdit) {
        await window.showErrorMessage("Error occurred during refactoring");
        return;
    }

    const edit = await p2cConverter.asWorkspaceEdit(lsEdit);

    await workspace.applyEdit(edit);
}

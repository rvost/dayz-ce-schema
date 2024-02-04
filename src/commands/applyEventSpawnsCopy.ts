import {Range, window, commands, workspace} from "vscode";
import {TextDocumentIdentifier, WorkspaceEdit} from "vscode-languageclient";
import {createConverter as createP2CConverter} from "vscode-languageclient/lib/common/protocolConverter";

const p2cConverter = createP2CConverter(undefined, true, true);

export const applyEventSpawnsCopyCommand = "dayz-ce-schema.applyEventSpawnsCopy";

export async function applyEventSpawnsCopyHandler(range: Range) {
    const destinations = [
        {label: "Top", description: "Copy to top of the document. May be overshadowed by existing spawns."},
        {label: "Bottom", description: "Copy to bottom of the document. May overwrite existing spawns."}
    ];
    const selectedOption = await window.showQuickPick(destinations, {
        placeHolder: "Peek copy destination",
        canPickMany: false
    });

    const prepend = selectedOption == destinations[0];
    const sourceDocument = window.activeTextEditor?.document;
    const identifier = TextDocumentIdentifier.create(sourceDocument!.uri.toString());

    const lsEdit: WorkspaceEdit | null = await commands.executeCommand(
        "xml.workspace.executeCommand",
        "dayz-ce-schema.computeEventsSpawnsCopy",
        identifier,
        range,
        prepend
    );

    if (!lsEdit) {
        await window.showErrorMessage("Error occurred during refactoring");
        return;
    }

    const edit = await p2cConverter.asWorkspaceEdit(lsEdit);

    await workspace.applyEdit(edit);
}

import * as vscode from "vscode";

import {SchemaAssociation, getSchemaForFileType} from "./xml/schemaAssociations";
import _defaultAssociations from "./xml/associations.json";
import {FILENAME_PATTERN, getCustomDefinitions, parseFile} from "./xml/cfgeconomycore";
import {makeGlobPattern, mergePatterns, readFileAsText} from "./utils";
import {documentationCommand, documentationHandler} from "./documentation/documentationProvider";
import {XMLExtensionApi} from "./xml/xmlExtensionApi";
import {applyCustomFilesRefactorCommand, applyCustomFilesRefactorHandler} from "./applyCustomFilesRefactor";
import {applyEventSpawnsCopyCommand, applyEventSpawnsCopyHandler} from "./applyEventSpawnsCopy";
import {applyExtractRandomPresetCommand, applyExtractRandomPresetHandler} from "./applyExtractRandomPreset";

const defaultSchemaAssociations = _defaultAssociations as SchemaAssociation[];

async function getCustomAssociations() {
    try {
        const files = await vscode.workspace.findFiles(FILENAME_PATTERN);

        const promises = files.map(async file => {
            const xml = await readFileAsText(file);
            const ceFile = await parseFile(xml);
            const definitions = getCustomDefinitions(ceFile);
            return definitions.map(({path, type}) => ({
                pattern: makeGlobPattern(path),
                systemId: getSchemaForFileType(type)
            }));
        });

        const customAssociations = await Promise.allSettled(promises).then(results =>
            results
                .filter(result => result.status === "fulfilled")
                .flatMap(result => (result as PromiseFulfilledResult<SchemaAssociation[]>).value)
        );

        return customAssociations;
    } catch (error) {
        return [];
    }
}

async function setupRedhatXml(inputFileAssociations: SchemaAssociation[]) {
    const redHatExtension = vscode.extensions.getExtension("redhat.vscode-xml");
    try {
        const extensionApi = (await redHatExtension!.activate()) as XMLExtensionApi;
        extensionApi.addXMLFileAssociations(inputFileAssociations);
    } catch (error) {
        let message;
        if (typeof error === "string") {
            message = error;
        } else if (error instanceof Error) {
            message = error.message;
        } else {
            message = "Unknown Error occurred";
        }

        await vscode.window.showErrorMessage(message);
    }
}

async function getAssociations() {
    const custom = await getCustomAssociations();
    const fileAssociations = mergePatterns([...defaultSchemaAssociations, ...custom]);
    return fileAssociations;
}

async function updateAssociations() {
    const redHatExtension = vscode.extensions.getExtension("redhat.vscode-xml");

    if (redHatExtension && redHatExtension.isActive) {
        const extensionApi = redHatExtension.exports as XMLExtensionApi;

        const fileAssociations = await getAssociations();
        extensionApi.removeXMLFileAssociations(fileAssociations);
        extensionApi.addXMLFileAssociations(fileAssociations);
    }
}

export async function activate(context: vscode.ExtensionContext) {
    const command = "dayz-ce-schema.updateCustomAssociations";
    context.subscriptions.push(vscode.commands.registerCommand(command, updateAssociations));
    context.subscriptions.push(vscode.commands.registerCommand(documentationCommand, documentationHandler));
    context.subscriptions.push(
        vscode.commands.registerCommand(applyCustomFilesRefactorCommand, applyCustomFilesRefactorHandler)
    );
    context.subscriptions.push(
        vscode.commands.registerCommand(applyEventSpawnsCopyCommand, applyEventSpawnsCopyHandler)
    );
    context.subscriptions.push(
        vscode.commands.registerCommand(applyExtractRandomPresetCommand, applyExtractRandomPresetHandler)
    );

    const fileAssociations = await getAssociations();
    await setupRedhatXml(fileAssociations);
}

export function deactivate() {}

import * as vscode from "vscode";

import { SchemaAssociation, defaultSchemaAssociations, getSchemaForFileType } from "./xml/schemaAssociations";
import { FILENAME_PATTERN, getCustomDefinitions, parseFile } from "./xml/cfgeconomycore";
import { makeGlobPattern, mergePatterns, readFileAsText } from "./utils";

async function getCustomAssociations() {
	try {
		const files = await vscode.workspace.findFiles(FILENAME_PATTERN);

		const promises = files.map(async file => {
			const xml = await readFileAsText(file);
			const ceFile = await parseFile(xml);
			const definitions = getCustomDefinitions(ceFile);
			return definitions.map(({ path, type }) => ({ pattern: makeGlobPattern(path), systemId: getSchemaForFileType(type) }));
		})

		const customAssociations = await Promise.allSettled(promises)
			.then(results => results.filter(result => result.status === "fulfilled")
				.flatMap(result => (result as PromiseFulfilledResult<SchemaAssociation[]>).value)
			)

		return customAssociations;
	}
	catch (error) {
		return [];
	}
}

async function setupRedhatXml(inputFileAssociations: SchemaAssociation[]) {
	const redHatExtension = vscode.extensions.getExtension("redhat.vscode-xml");
	try {
		const extensionApi = await redHatExtension!.activate();
		extensionApi.addXMLFileAssociations(inputFileAssociations);
	} catch (error) {
		let message;
		if (typeof error === "string") {
			message = error;
		} else if (error instanceof Error) {
			message = error.message;
		}
		else {
			message = "Unknown Error occurred";
		}

		vscode.window.showErrorMessage(message);
	}
}

export async function activate(context: vscode.ExtensionContext) {
	const custom = await getCustomAssociations();
	const fileAssociations = mergePatterns([...defaultSchemaAssociations, ...custom]);

	await setupRedhatXml(fileAssociations);
}

export function deactivate() { }

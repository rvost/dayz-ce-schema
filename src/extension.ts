import * as vscode from 'vscode';
import { remoteSchemaAssociations } from './schemaAssociations';

async function setupRedhatXml(inputFileAssociations: Array<{ systemId: string; pattern: string }>) {
	const redHatExtension = vscode.extensions.getExtension('redhat.vscode-xml');
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
		else{
			message = "Unknown Error occurred";
		}

		vscode.window.showErrorMessage(message);
	}
}

export async function activate(context: vscode.ExtensionContext) {
	await setupRedhatXml(remoteSchemaAssociations);
}

export function deactivate() { }

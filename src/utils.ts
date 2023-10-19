import * as path from "path";
import * as vscode from "vscode";

import {SchemaAssociation} from "./xml/schemaAssociations";

export function makeGlobPattern(filePath: string) {
    // patterns in redhat.vscode-xml use '/' regardless of the platform
    return ["**", ...filePath.split(path.sep)].join("/");
}

export function mergePatterns(input: SchemaAssociation[]): SchemaAssociation[] {
    const groups = input.reduce(
        (entryMap, e) => entryMap.set(e.systemId, [...(entryMap.get(e.systemId) || []), e.pattern]),
        new Map<string, string[]>()
    );

    const result = [];

    for (const [systemId, patterns] of groups) {
        result.push({systemId, pattern: `{${patterns.join(",")}}`});
    }

    return result;
}

export async function readFileAsText(uri: vscode.Uri) {
    const data = await vscode.workspace.fs.readFile(uri);
    return Buffer.from(data).toString("utf8");
}

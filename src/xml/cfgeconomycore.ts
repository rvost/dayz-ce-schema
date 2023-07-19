import * as path from "path";
import { Parser } from "xml2js";

export const FILENAME_PATTERN = "**/cfgeconomycore.xml"

const VALID_TYPES = new Set(["types", "spawnabletypes", "globals", "economy", "events", "messages"])

type CfgEconomyCore = {
    economycore: {
        ce?: Array<{
            $: {
                folder: string
            }
            file?: Array<{
                $: {
                    name: string
                    type: string
                }
            }>
        }>
    }
}

export async function parseFile(xml: string) {
    const parser = new Parser();
    return await parser.parseStringPromise(xml) as CfgEconomyCore;
}

export function getCustomDefinitions(file: CfgEconomyCore): { path: string, type: string }[] {
    const folders = file.economycore.ce ?? [];

    return folders
        .flatMap(ceFolder => {
            const files = ceFolder.file ?? [];
            const folder = ceFolder.$.folder ?? "./";

            return files
                .filter(file => file.$.name && file.$.type)
                .map(file => {
                    const filePath = path.join(folder, file.$.name ?? "");
                    return { path: filePath, type: file.$.type ?? "" };
                });
        })
        .filter(({ type }) => VALID_TYPES.has(type));
}

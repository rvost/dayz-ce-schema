import * as vscode from "vscode";
import { minimatch } from "minimatch";

const associations = [
    {
        pattern: "**/cfgeffectarea.json",
        url: "https://community.bistudio.com/wiki/DayZ:Contaminated_Areas_Configuration"
    },
    {
        pattern: "**/cfggameplay.json",
        url: "https://community.bistudio.com/wiki/DayZ:Gameplay_Settings"
    },
    {
        pattern: "**/cfgundergroundtriggers.json",
        url: "https://community.bistudio.com/wiki/DayZ:Underground_Areas_Configuration"
    },
    {
        pattern: "**/cfgeconomycore.xml",
        url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding"
    },
    {
        pattern: "**/cfgweather.xml",
        url: "https://community.bistudio.com/wiki/DayZ:Weather_Configuration"
    },
    {
        pattern: "**/db/globals.xml",
        url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_Configuration#db.5Cglobals.xml"
    },
    {
		pattern: "**/db/economy.xml",
		url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
	},
	{
		pattern: "**/db/events.xml",
		url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
	},
    {
		pattern: "**/db/messages.xml",
		url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
	},
    {
		pattern: "**/types*.xml",
		url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
	},
    {
		pattern: "**/cfgspawnabletypes.xml",
		url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
	},
]

export const documentationCommand = "dayz-ce-schema.showDocumentation";

export function documentationHandler() {
    const activeFileName = vscode.window.activeTextEditor?.document?.fileName;
    if (!activeFileName) {
        return;
    }

    let a = associations.find(a => minimatch(activeFileName, a.pattern, { nocase: true }))
    if (a?.url) {
        vscode.env.openExternal(vscode.Uri.parse(a.url));
    }
    else{
        vscode.window.showInformationMessage("No documentation available for this file");
    }
}
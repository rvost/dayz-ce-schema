import * as vscode from "vscode";
import { minimatch } from "minimatch";

type DocumentationLink = { title: string, url: string };

type DocumentationAssociation = {
    pattern: string,
    links: DocumentationLink[]
};

const associations: DocumentationAssociation[] = [
    {
        pattern: "**/cfgeffectarea.json",
        links: [
            {
                title: "Wiki Page: Contaminated Areas Configuration",
                url: "https://community.bistudio.com/wiki/DayZ:Contaminated_Areas_Configuration"
            }
        ]
    },
    {
        pattern: "**/cfggameplay.json",
        links: [
            {
                title: "Wiki Page: Gameplay Settings",
                url: "https://community.bistudio.com/wiki/DayZ:Gameplay_Settings"
            }
        ]
    },
    {
        pattern: "**/cfgundergroundtriggers.json",
        links: [
            {
                title: "Wiki Page: Underground Areas Configuration",
                url: "https://community.bistudio.com/wiki/DayZ:Underground_Areas_Configuration"
            }
        ]
    },
    {
        pattern: "**/cfgeconomycore.xml",
        links: [
            {
                title: "Wiki Page: Central Economy Configuration",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_Configuration#cfgEconomyCore.xml"
            },
            {
                title: "Wiki Page: Central Economy mission files modding",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding"
            }
        ]
    },
    {
        pattern: "**/cfgweather.xml",
        links: [
            {
                title: "Wiki Page: Weather Configuration",
                url: "https://community.bistudio.com/wiki/DayZ:Weather_Configuration"
            }
        ]
    },
    {
        pattern: "**/db/globals.xml",
        links: [
            {
                title: "Wiki Page: Central Economy Configuration",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_Configuration#db.5Cglobals.xml"
            }
        ]
    },
    {
        pattern: "**/db/economy.xml",
        links: [
            {
                title: "Wiki Page: Central Economy mission files modding",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
            }
        ]
    },
    {
        pattern: "**/db/events.xml",
        links: [
            {
                title: "Wiki Page: Central Economy mission files modding",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
            }
        ]
    },
    {
        pattern: "**/db/messages.xml",
        links: [
            {
                title: "Wiki Page: Central Economy mission files modding",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
            }
        ]
    },
    {
        pattern: "**/types*.xml",
        links: [
            {
                title: "Wiki Page: Central Economy mission files modding",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
            },
            {
                title: "DayZ Forum thread: types.xml explanation",
                url: "https://forums.dayz.com/topic/247502-typesxml-need-explanation/"
            }
        ]
    },
    {
        pattern: "**/cfgspawnabletypes.xml",
        links: [
            {
                title: "Wiki Page: Central Economy mission files modding",
                url: "https://community.bistudio.com/wiki/DayZ:Central_Economy_mission_files_modding#File_specifics"
            }
        ]
    },
];

function toQuickPickItem(link: DocumentationLink) {
    return {
        label: link.title,
        detail: link.url,
        url: link.url
    }
}

export const documentationCommand = "dayz-ce-schema.showDocumentation";

export async function documentationHandler() {
    const activeFileName = vscode.window.activeTextEditor?.document?.fileName;
    if (!activeFileName) {
        return;
    }

    let matches = associations
        .filter(a => minimatch(activeFileName, a.pattern, { nocase: true }))
        .flatMap(a => a.links);

    if (!matches) {
        vscode.window.showInformationMessage("No documentation available for this file");
        return;
    }

    if (matches.length == 1) {
        vscode.env.openExternal(vscode.Uri.parse(matches[0].url));
        return;
    }
    else {
        const items = matches.map(toQuickPickItem);
        const item = await vscode.window.showQuickPick(items, {placeHolder: "Pick page to open"});
        
        if (item) {
            vscode.env.openExternal(vscode.Uri.parse(item.url));
        }
    }
}
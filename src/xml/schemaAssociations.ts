export type SchemaAssociation = {pattern: string; systemId: string};

export function getSchemaForFileType(type: string) {
    switch (type) {
        case "types":
            return "https://rvost.github.io/DayZ-Central-Economy-Schema/db/types.xsd";
        case "spawnabletypes":
            return "https://rvost.github.io/DayZ-Central-Economy-Schema/cfgspawnabletypes.xsd";
        case "globals":
            return "https://rvost.github.io/DayZ-Central-Economy-Schema/db/globals.xsd";
        case "economy":
            return "https://rvost.github.io/DayZ-Central-Economy-Schema/db/economy.xsd";
        case "events":
            return "https://rvost.github.io/DayZ-Central-Economy-Schema/db/events.xsd";
        case "messages":
            return "https://rvost.github.io/DayZ-Central-Economy-Schema/db/messages.xsd";
        default:
            return "";
    }
}

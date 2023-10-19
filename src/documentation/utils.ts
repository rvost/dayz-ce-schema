import {DocumentationLink, DocumentationAssociation} from "./types";

export function toQuickPickItem(link: DocumentationLink) {
    return {
        label: link.title,
        detail: link.url,
        url: link.url
    };
}

export function toMap(associations: DocumentationAssociation[]): Map<string, DocumentationLink[]> {
    const result = new Map();
    associations.forEach(x => result.set(x.pattern, x.links));
    return result;
}

export function combine<T>(map1: Map<string, T[]>, map2: Map<string, T[]>): Map<string, T[]> {
    let result = new Map();

    for (let [key, value] of map1) {
        if (map2.has(key)) {
            result.set(key, value.concat(map2.get(key) || []));
        } else {
            result.set(key, value);
        }
    }

    for (let [key, value] of map2) {
        if (!result.has(key)) {
            result.set(key, value);
        }
    }

    return result;
}

package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class SpawnableTypesModel {
    public static final String ATTACHMENTS_TAG = "attachments";
    public static final String CARGO_TAG = "cargo";
    public static final String SPAWNABLETYPES_TAG = "spawnabletypes";
    public static final String PRESET_ATTRIBUTE = "preset";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String SPAWNABLETYPES_FILE = "cfgspawnabletypes.xml";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.documentTagMatch(document, SPAWNABLETYPES_TAG);
    }

    public static Map<String, Range> getSpawnableTypes(Path path) {
        return DocumentUtils.tryParseDocument(path)
                .map(SpawnableTypesModel::getSpawnableTypes)
                .orElse(Map.of());
    }

    public static Map<String, Range> getSpawnableTypes(DOMDocument doc) {
        return DocumentUtils.indexByAttribute(doc, NAME_ATTRIBUTE);
    }

    public static Map<String, List<Range>> getPresetsIndex(Path path) {
        return DocumentUtils.tryParseDocument(path)
                .map(SpawnableTypesModel::getPresetsIndex)
                .orElse(Map.of());
    }

    public static Map<String, List<Range>> getPresetsIndex(DOMDocument document) {
        return DocumentUtils.indexChildrenByAttribute(document, PRESET_ATTRIBUTE);
    }
}

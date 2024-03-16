package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static Map<String, Range> getSpawnableTypes(DOMDocument doc) {
        return DocumentUtils.indexByAttribute(doc, NAME_ATTRIBUTE);
    }

    public static Map<String, List<Range>> getPresetsIndex(Path path) {
        return DocumentUtils.tryParseDocument(path)
                .map(SpawnableTypesModel::getPresetsIndex)
                .orElse(Map.of());
    }

    public static Map<String, List<Range>> getPresetsIndex(DOMDocument document) {
        return document.getDocumentElement().getChildren().stream()
                .flatMap(x -> x.getChildren().stream())
                .filter(x -> x.hasAttribute(PRESET_ATTRIBUTE))
                .collect(Collectors.groupingBy(
                        x -> x.getAttribute(PRESET_ATTRIBUTE),
                        Collectors.mapping(
                                x -> XMLPositionUtility.selectWholeTag(x.getStart() + 1, document),
                                Collectors.toList()
                        ))
                );
    }
}

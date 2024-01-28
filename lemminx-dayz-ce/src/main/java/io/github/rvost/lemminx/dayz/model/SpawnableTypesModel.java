package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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

    public static boolean isSpawnableTypes(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && SPAWNABLETYPES_TAG.equals(docElement.getNodeName());
    }

    public static Map<String, Range> getSpawnableTypes(Path path) {
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(path));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, path.toString()), null);
            return getSpawnableTypes(doc);
        } catch (IOException e) {
            return Map.of();
        }
    }

    private static Map<String, Range> getSpawnableTypes(DOMDocument doc) {
        return doc.getDocumentElement().getChildren().stream()
                .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                .map(n -> n.getAttributeNode(NAME_ATTRIBUTE))
                .collect(Collectors.toMap(
                        DOMAttr::getNodeValue,
                        n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                        (oldValue, newValue) -> newValue,
                        HashMap::new));
    }

    public static Map<String, List<Range>> getPresetsIndex(Path path) {
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(path));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, path.toString()), null);
            return getPresetsIndex(doc);
        } catch (IOException e) {
            return Map.of();
        }
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

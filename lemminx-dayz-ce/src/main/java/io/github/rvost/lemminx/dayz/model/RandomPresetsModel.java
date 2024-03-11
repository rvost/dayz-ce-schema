package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class RandomPresetsModel {
    public static final String CFGRANDOMPRESETS_FILE = "cfgrandompresets.xml";
    public static final String CARGO_TAG = "cargo";
    public static final String ATTACHMENTS_TAG = "attachments";
    public static final String ITEM_TAG = "item";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, CFGRANDOMPRESETS_FILE);
    }

    public static Map<String, Set<String>> getRandomPresets(Path missionPath) {
        var path = missionPath.resolve(CFGRANDOMPRESETS_FILE);
        return DocumentUtils.tryParseDocument(path)
                .map(RandomPresetsModel::getRandomPresets)
                .orElse(Map.of());
    }

    public static Map<String, Set<String>> getRandomPresets(DOMDocument doc) {
        var cargo = doc.getDocumentElement().getChildren().stream()
                .filter(n -> CARGO_TAG.equals(n.getLocalName()))
                .map(n -> n.getAttribute(NAME_ATTRIBUTE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        var attachments = doc.getDocumentElement().getChildren().stream()
                .filter(n -> ATTACHMENTS_TAG.equals(n.getLocalName()))
                .map(n -> n.getAttribute(NAME_ATTRIBUTE))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return new HashMap<>(Map.ofEntries(
                entry(CARGO_TAG, cargo),
                entry(ATTACHMENTS_TAG, attachments)
        ));
    }

    public static Map<String, Range> getRandomPresetsIndex(Path missionPath) {
        var filePath = missionPath.resolve(CFGRANDOMPRESETS_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(RandomPresetsModel::getRandomPresetsIndex)
                .orElse(Map.of());
    }

    private static Map<String, Range> getRandomPresetsIndex(DOMDocument doc) {
        return doc.getDocumentElement().getChildren().stream()
                .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                .map(n -> n.getAttributeNode(NAME_ATTRIBUTE))
                .collect(Collectors.toMap(
                        DOMAttr::getNodeValue,
                        n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                        (oldValue, newValue) -> oldValue,
                        HashMap::new));
    }
}

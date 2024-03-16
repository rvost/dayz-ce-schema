package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CfgEventSpawnsModel {
    public static final String CFGEVENTSPAWNS_FILE = "cfgeventspawns.xml";
    public static final String GROUP_ATTRIBUTE = "group";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, CFGEVENTSPAWNS_FILE);
    }

    public static Map<String, Range> getCfgEventSpawns(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTSPAWNS_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(CfgEventSpawnsModel::getCfgEventSpawns)
                .orElse(Map.of());
    }

    private static Map<String, Range> getCfgEventSpawns(DOMDocument doc) {
        return DocumentUtils.indexByAttribute(doc, NAME_ATTRIBUTE);
    }

    public static Map<String, Range> getCfgEventSpawnsGroupReferences(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTSPAWNS_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(CfgEventSpawnsModel::getEventSpawnGroupReferences)
                .orElse(Map.of());
    }

    private static Map<String, Range> getEventSpawnGroupReferences(DOMDocument doc) {
        return doc.getDocumentElement().getChildren().stream()
                .flatMap(n -> n.getChildren().stream())
                .filter(n -> n.hasAttribute(GROUP_ATTRIBUTE))
                .map(n -> n.getAttributeNode(GROUP_ATTRIBUTE))
                .collect(Collectors.toMap(
                        DOMAttr::getNodeValue,
                        n -> XMLPositionUtility.selectWholeTag(n.getStart() + 1, doc),
                        (oldValue, newValue) -> oldValue,
                        HashMap::new));
    }
}

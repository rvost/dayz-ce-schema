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

public class MapGroupProtoModel {
    public static final String MAPGROUPPROTO_FILE = "mapgroupproto.xml";
    public static final String DEFAULT_TAG = "default";
    public static final String GROUP_TAG = "group";
    public static final String CONTAINER_TAG = "container";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_ATTRIBUTE = "user";
    public static final String DE_ATTRIBUTE = "de";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, MAPGROUPPROTO_FILE);
    }

    public static Map<String, Range> getGroups(Path missionPath) {
        var path = missionPath.resolve(MAPGROUPPROTO_FILE);
        return DocumentUtils.tryParseDocument(path)
                .map(MapGroupProtoModel::getGroups)
                .orElse(Map.of());
    }

    private static Map<String, Range> getGroups(DOMDocument doc) {
        return doc.getDocumentElement().getChildren().stream()
                .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                .map(n -> n.getAttributeNode(NAME_ATTRIBUTE))
                .collect(Collectors.toMap(
                        DOMAttr::getNodeValue,
                        n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                        (oldValue, newValue) -> newValue,
                        HashMap::new));
    }
}

package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CfgEventGroupsModel {
    public static final String CFGEVENTGROUPS_FILE = "cfgeventgroups.xml";
    public static final String GROUP_TAG = "group";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String SPAWNSECONDARY_ATTRIBUTE = "spawnsecondary";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, CFGEVENTGROUPS_FILE);
    }

    public static Map<String, Range> getCfgEventGroups(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTGROUPS_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(CfgEventGroupsModel::getCfgEventGroups)
                .orElse(Map.of());
    }

    private static Map<String, Range> getCfgEventGroups(DOMDocument doc) {
        return doc.getDocumentElement().getChildren().stream()
                .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                .map(n -> n.getAttributeNode(NAME_ATTRIBUTE))
                .collect(Collectors.toMap(
                        DOMAttr::getNodeValue,
                        n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                        (oldValue, newValue) -> oldValue,
                        HashMap::new));
    }

    public static Map<String, List<Range>> getChildTypesIndex(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTGROUPS_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(CfgEventGroupsModel::getChildTypesIndex)
                .orElse(Map.of());
    }

    private static Map<String, List<Range>> getChildTypesIndex(DOMDocument doc) {
        return doc.getDocumentElement().getChildren().stream()
                .flatMap(n -> n.getChildren().stream())
                .filter(n -> n.hasAttribute(TYPE_ATTRIBUTE))
                .map(n -> n.getAttributeNode(TYPE_ATTRIBUTE))
                .collect(Collectors.groupingBy(
                        DOMAttr::getNodeValue,
                        Collectors.mapping(
                                n -> XMLPositionUtility.selectWholeTag(n.getStart() + 1, doc),
                                Collectors.toList()))
                );
    }
}

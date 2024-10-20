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

public class CfgEventGroupsModel {
    public static final String CFGEVENTGROUPS_FILE = "cfgeventgroups.xml";
    public static final String GROUP_TAG = "group";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String SPAWNSECONDARY_ATTRIBUTE = "spawnsecondary";

    public static boolean isCfgEventGroups(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(CFGEVENTGROUPS_FILE);
    }

    public static Map<String, Range> getCfgEventGroups(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTGROUPS_FILE);
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(filePath));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, filePath.toString()), null);
            return doc.getDocumentElement().getChildren().stream()
                    .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                    .map(n -> n.getAttributeNode(NAME_ATTRIBUTE))
                    .collect(Collectors.toMap(
                            DOMAttr::getNodeValue,
                            n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                            (oldValue, newValue) -> oldValue,
                            HashMap::new));
        } catch (IOException e) {
            return Map.of();
        }
    }

    public static Map<String, List<Range>> getChildTypesIndex(Path missionPath) {
        var filePath = missionPath.resolve(CFGEVENTGROUPS_FILE);
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(filePath));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, filePath.toString()), null);
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
        } catch (IOException e) {
            return Map.of();
        }
    }
}

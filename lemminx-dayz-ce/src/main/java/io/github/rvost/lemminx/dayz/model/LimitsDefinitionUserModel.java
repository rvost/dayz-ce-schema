package io.github.rvost.lemminx.dayz.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class LimitsDefinitionUserModel {
    public static final String USER_LIMITS_DEFINITION_FILE = "cfglimitsdefinitionuser.xml";
    public static final String USAGEFLAGS_TAG = "usageflags";
    public static final String VALUEFLAGS_TAG = "valueflags";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_TAG = "user";

    public static boolean isUserLimitsDefinitions(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(USER_LIMITS_DEFINITION_FILE);
    }

    public static Map<String, Set<String>> getUserLimitsDefinitions(Path missionPath) {
        var path = missionPath.resolve(USER_LIMITS_DEFINITION_FILE);

        try (var input = Files.newInputStream(path)) {
            return getUserLimitsDefinitions(input);
        } catch (IOException e) {
            return Map.of();
        }
    }

    public static Map<String, Set<String>> getUserLimitsDefinitions(InputStream input) throws IOException {
        try {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                var usageflagsNode = doc.getElementsByTagName(USAGEFLAGS_TAG).item(0);
                var valueflagsNode = doc.getElementsByTagName(VALUEFLAGS_TAG).item(0);
                var result = new HashMap<String, Set<String>>();
                if (usageflagsNode != null) {
                    var usageflags = LimitsDefinitionsModel.getValues(usageflagsNode.getChildNodes());
                    result.put(LimitsDefinitionsModel.USAGE_TAG, usageflags);
                }
                if (valueflagsNode != null) {
                    var valueflags = LimitsDefinitionsModel.getValues(valueflagsNode.getChildNodes());
                    result.put(LimitsDefinitionsModel.VALUE_TAG, valueflags);
                }
                return result;
            } else {
                return Map.of();
            }
        } catch (ParserConfigurationException | SAXException e) {
            return Map.of();
        }
    }

    public static BiMap<String, Set<String>> getUserFlags(Path missionPath) {
        var path = missionPath.resolve(USER_LIMITS_DEFINITION_FILE);

        try (var input = Files.newInputStream(path)) {
            return getUserFlags(input);
        } catch (IOException e) {
            return ImmutableBiMap.of();
        }
    }

    public static BiMap<String, Set<String>> getUserFlags(InputStream input) throws IOException {
        try {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                var usageflagsNode = doc.getElementsByTagName(USAGEFLAGS_TAG).item(0);
                var valueflagsNode = doc.getElementsByTagName(VALUEFLAGS_TAG).item(0);
                BiMap<String, Set<String>> result = HashBiMap.create();
                if (usageflagsNode != null) {
                    var usageflags = getFlagDefinitions(usageflagsNode.getChildNodes());
                    result.putAll(usageflags);
                }
                if (valueflagsNode != null) {
                    var valueflags = getFlagDefinitions(valueflagsNode.getChildNodes());
                    result.putAll(valueflags);
                }
                return result;
            } else {
                return ImmutableBiMap.of();
            }
        } catch (ParserConfigurationException | SAXException e) {
            return ImmutableBiMap.of();
        }
    }

    private static Map<String, Set<String>> getFlagDefinitions(NodeList nodes) {
        var result = new HashMap<String, Set<String>>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);
            var attributes = node.getAttributes();
            if (attributes != null) {
                var nameAttr = attributes.getNamedItem(LimitsDefinitionsModel.NAME_ATTRIBUTE);
                if (nameAttr != null) {
                    var flagName = nameAttr.getNodeValue();
                    var flagValues = new TreeSet<>(LimitsDefinitionsModel.getOrderedValues(node.getChildNodes()));
                    result.put(flagName, flagValues);
                }
            }
        }
        return result;
    }

    public static Map<String, Range> getUserFlagsIndex(Path missionPath) {
        var filePath = missionPath.resolve(USER_LIMITS_DEFINITION_FILE);
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(filePath));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, filePath.toString()), null);
            return doc.getDocumentElement().getChildren().stream()
                    .flatMap(n -> n.getChildren().stream())
                    .filter(n -> n.hasAttribute(LimitsDefinitionsModel.NAME_ATTRIBUTE))
                    .map(n -> n.getAttributeNode(LimitsDefinitionsModel.NAME_ATTRIBUTE))
                    .collect(Collectors.toMap(
                            DOMAttr::getNodeValue,
                            n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                            (oldValue, newValue) -> oldValue,
                            HashMap::new));
        } catch (IOException e) {
            return Map.of();
        }
    }
}

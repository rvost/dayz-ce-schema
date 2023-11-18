package io.github.rvost.lemminx.dayz.model;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public class LimitsDefinitionsModel {
    public static final String LIMITS_DEFINITION_FILE = "cfglimitsdefinition.xml";
    public static final String USER_LIMITS_DEFINITION_FILE = "cfglimitsdefinitionuser.xml";
    public static final String CATEGORY_TAG = "category";
    public static final String TAG_TAG = "tag";
    public static final String USAGE_TAG = "usage";
    public static final String VALUE_TAG = "value";
    public static final String USAGEFLAGS_TAG = "usageflags";
    public static final String VALUEFLAGS_TAG = "valueflags";
    public static final String NAME_ATTRIBUTE = "name";

    public static Map<String, Set<String>> getLimitsDefinitions(Path missionPath) {
        var path = missionPath.resolve(LIMITS_DEFINITION_FILE);

        try (var input = Files.newInputStream(path)) {
            return getLimitsDefinitions(input);
        } catch (IOException e) {
            return Map.of();
        }
    }

    public static Map<String, Set<String>> getLimitsDefinitions(InputStream input) throws IOException {
        try {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                var categories = getValues(doc.getElementsByTagName(CATEGORY_TAG));
                var tags = getValues(doc.getElementsByTagName(TAG_TAG));
                var usages = getValues(doc.getElementsByTagName(USAGE_TAG));
                var values = getValues(doc.getElementsByTagName(VALUE_TAG));
                return new HashMap<>(Map.ofEntries(
                        entry(CATEGORY_TAG, categories),
                        entry(TAG_TAG, tags),
                        entry(USAGE_TAG, usages),
                        entry(VALUE_TAG, values)
                ));
            } else {
                return Map.of();
            }
        } catch (ParserConfigurationException | SAXException e) {
            return Map.of();
        }
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
                    var usageflags = getValues(usageflagsNode.getChildNodes());
                    result.put(USAGE_TAG, usageflags);
                }
                if (valueflagsNode != null) {
                    var valueflags = getValues(valueflagsNode.getChildNodes());
                    result.put(VALUE_TAG, valueflags);
                }
                return result;
            } else {
                return Map.of();
            }
        } catch (ParserConfigurationException | SAXException e) {
            return Map.of();
        }
    }

    private static Set<String> getValues(NodeList nodes) {
        var result = new HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);
            var attributes = node.getAttributes();
            if (attributes != null) {
                var nameAttr = attributes.getNamedItem(NAME_ATTRIBUTE);
                if (nameAttr != null) {
                    result.add(nameAttr.getNodeValue());
                }
            }
        }
        return result;
    }
}

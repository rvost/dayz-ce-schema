package io.github.rvost.lemminx.dayz.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import io.github.rvost.lemminx.dayz.participants.DOMUtils;
import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class LimitsDefinitionUserModel {
    public static final String USER_LIMITS_DEFINITION_FILE = "cfglimitsdefinitionuser.xml";
    public static final String USAGEFLAGS_TAG = "usageflags";
    public static final String VALUEFLAGS_TAG = "valueflags";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_TAG = "user";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, USER_LIMITS_DEFINITION_FILE);
    }

    public static Map<String, Set<String>> getUserLimitsDefinitions(Path missionPath) {
        var path = missionPath.resolve(USER_LIMITS_DEFINITION_FILE);
        return DocumentUtils.tryParseDocument(path)
                .map(LimitsDefinitionUserModel::getUserLimitsDefinitions)
                .orElse(Map.of());
    }

    public static Map<String, Set<String>> getUserLimitsDefinitions(DOMDocument doc) {
        var root = doc.getDocumentElement();
        var usageflags = DOMUtils.tryFindFirstChildElementByTagName(root, USAGEFLAGS_TAG)
                .map(LimitsDefinitionsModel::getValues)
                .orElse(Set.of());
        var valueflags = DOMUtils.tryFindFirstChildElementByTagName(root, VALUEFLAGS_TAG)
                .map(LimitsDefinitionsModel::getValues)
                .orElse(Set.of());
        return new HashMap<>(Map.ofEntries(
                entry(LimitsDefinitionsModel.USAGE_TAG, usageflags),
                entry(LimitsDefinitionsModel.VALUE_TAG, valueflags)
        ));
    }

    public static BiMap<String, Set<String>> getUserFlags(Path missionPath) {
        var path = missionPath.resolve(USER_LIMITS_DEFINITION_FILE);
        return DocumentUtils.tryParseDocument(path)
                .map(LimitsDefinitionUserModel::getUserFlags)
                .orElse(ImmutableBiMap.of());
    }

    public static BiMap<String, Set<String>> getUserFlags(DOMDocument doc) {
        var root = doc.getDocumentElement();
        var usageflags = DOMUtils.tryFindFirstChildElementByTagName(root, USAGEFLAGS_TAG)
                .map(LimitsDefinitionUserModel::getFlagDefinitions)
                .orElse(Map.of());
        var valueflags = DOMUtils.tryFindFirstChildElementByTagName(root, VALUEFLAGS_TAG)
                .map(LimitsDefinitionUserModel::getFlagDefinitions)
                .orElse(Map.of());
        BiMap<String, Set<String>> result = HashBiMap.create();
        result.putAll(usageflags);
        result.putAll(valueflags);
        return result;
    }

    private static Map<String, Set<String>> getFlagDefinitions(DOMElement element) {
        return element.getChildren().stream()
                .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                .map(n -> Map.entry(
                        n.getAttribute(NAME_ATTRIBUTE),
                        LimitsDefinitionsModel.getOrderedValues((DOMElement) n))
                )
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new TreeSet<>(e.getValue()),
                        (old, newV) -> newV,
                        HashMap::new)
                );
    }

    public static Map<String, Range> getUserFlagsIndex(Path missionPath) {
        var filePath = missionPath.resolve(USER_LIMITS_DEFINITION_FILE);
        return DocumentUtils.tryParseDocument(filePath)
                .map(doc -> doc.getDocumentElement().getChildren().stream()
                        .flatMap(n -> n.getChildren().stream())
                        .filter(n -> n.hasAttribute(LimitsDefinitionsModel.NAME_ATTRIBUTE))
                        .map(n -> n.getAttributeNode(LimitsDefinitionsModel.NAME_ATTRIBUTE))
                        .collect(Collectors.toMap(
                                DOMAttr::getNodeValue,
                                n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                                (oldValue, newValue) -> oldValue,
                                HashMap::new)))
                .orElse(new HashMap<>());
    }
}

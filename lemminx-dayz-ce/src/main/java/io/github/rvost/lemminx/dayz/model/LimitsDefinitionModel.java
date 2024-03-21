package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.participants.DOMUtils;
import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;

import java.nio.file.Path;
import java.util.*;

import static java.util.Map.entry;

public class LimitsDefinitionModel {
    public static final String LIMITS_DEFINITION_FILE = "cfglimitsdefinition.xml";
    public static final String CATEGORIES_TAG = "categories";
    public static final String TAGS_TAG = "tags";
    public static final String USAGEFLAGS_TAG = "usageflags";
    public static final String VALUEFLAGS_TAG = "valueflags";
    public static final String CATEGORY_TAG = "category";
    public static final String TAG_TAG = "tag";
    public static final String USAGE_TAG = "usage";
    public static final String VALUE_TAG = "value";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, LIMITS_DEFINITION_FILE);
    }

    public static Map<String, Set<String>> getLimitsDefinitions(Path missionPath) {
        var path = missionPath.resolve(LIMITS_DEFINITION_FILE);
        return DocumentUtils.tryParseDocument(path)
                .map(LimitsDefinitionModel::getLimitsDefinitions)
                .orElse(Map.of());
    }

    public static Map<String, Set<String>> getLimitsDefinitions(DOMDocument doc) {
        var root = doc.getDocumentElement();
        var categories = DOMUtils.tryFindFirstChildElementByTagName(root, CATEGORIES_TAG)
                .map(LimitsDefinitionModel::getValues)
                .orElse(Set.of());
        var tags = DOMUtils.tryFindFirstChildElementByTagName(root, TAGS_TAG)
                .map(LimitsDefinitionModel::getValues)
                .orElse(Set.of());
        var usages = DOMUtils.tryFindFirstChildElementByTagName(root, USAGEFLAGS_TAG)
                .map(LimitsDefinitionModel::getValues)
                .orElse(Set.of());
        var values = DOMUtils.tryFindFirstChildElementByTagName(root, VALUEFLAGS_TAG)
                .map(LimitsDefinitionModel::getValues)
                .orElse(Set.of());
        return new HashMap<>(Map.ofEntries(
                entry(CATEGORY_TAG, categories),
                entry(TAG_TAG, tags),
                entry(USAGE_TAG, usages),
                entry(VALUE_TAG, values)
        ));
    }

    static Set<String> getValues(DOMElement element) {
        return new HashSet<>(getOrderedValues(element));
    }

    static List<String> getOrderedValues(DOMElement element) {
        return element.getChildren().stream()
                .map(e -> e.getAttribute(NAME_ATTRIBUTE))
                .filter(Objects::nonNull)
                .toList();
    }
}

package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TypesModel {
    public static final String DB_FOLDER = "db";
    public static final String TYPES_FILE = "types.xml";
    public static final String TYPES_TAG = "types";
    public static final String TYPE_TAG = "type";
    public static final String USAGE_TAG = "usage";
    public static final String VALUE_TAG = "value";
    public static final String TAG_TAG = "tag";
    public static final String CATEGORY_TAG = "category";
    public static final String LIFETIME_TAG = "lifetime";
    public static final String RESTOCK_TAG = "restock";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_ATTRIBUTE = "user";
    public static final Set<String> LIMITS_TAGS = Set.of(USAGE_TAG, VALUE_TAG, TAG_TAG, CATEGORY_TAG);
    public static final Set<String> USER_LIMITS_TAGS = Set.of(USAGE_TAG, VALUE_TAG);
    public static final Set<String> TIME_INTERVAL_TAGS = Set.of(LIFETIME_TAG, RESTOCK_TAG);
    public static final Path rootTypesPath = Path.of(TypesModel.DB_FOLDER, TypesModel.TYPES_FILE);

    public static boolean match(DOMDocument document) {
        return DocumentUtils.documentTagMatch(document, TYPES_TAG);
    }

    public static boolean isRootTypes(Path missionRoot, Path file) {
        var relative = missionRoot.relativize(file);
        return rootTypesPath.equals(relative);
    }

    public static Map<String, Range> getRootTypes(Path missionPath) {
        var filePath = missionPath.resolve(rootTypesPath);
        return getTypes(filePath);
    }

    public static Map<String, Range> getTypes(Path path) {
        return DocumentUtils.tryParseDocument(path)
                .map(TypesModel::getTypes)
                .orElse(Map.of());
    }

    public static Map<String, Range> getTypes(DOMDocument doc) {
        return DocumentUtils.indexByAttribute(doc, NAME_ATTRIBUTE);
    }

    public static Map<String, List<Range>> getFlagIndex(Path path) {
        return DocumentUtils.tryParseDocument(path)
                .map(TypesModel::getFlagIndex)
                .orElse(Map.of());
    }

    public static Map<String, List<Range>> getFlagIndex(DOMDocument document) {
        return document.getDocumentElement().getChildren().stream()
                .flatMap(x -> x.getChildren().stream())
                .filter(n -> LIMITS_TAGS.stream().anyMatch(tag -> tag.equals(n.getLocalName())))
                .filter(x -> x.hasAttribute(NAME_ATTRIBUTE))
                .collect(Collectors.groupingBy(
                        x -> x.getAttribute(NAME_ATTRIBUTE),
                        Collectors.mapping(
                                x -> XMLPositionUtility.selectWholeTag(x.getStart() + 1, document),
                                Collectors.toList()
                        ))
                );
    }

    public static Map<String, List<Range>> getUserFlagIndex(Path path) {
        return DocumentUtils.tryParseDocument(path)
                .map(TypesModel::getUserFlagIndex)
                .orElse(Map.of());
    }

    public static Map<String, List<Range>> getUserFlagIndex(DOMDocument document) {
        return DocumentUtils.indexChildrenByAttribute(document, USER_ATTRIBUTE);
    }
}

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

    public static boolean isTypes(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && TYPES_TAG.equals(docElement.getNodeName());
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
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(path));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, path.toString()), null);
            return getTypes(doc);
        } catch (IOException e) {
            return Map.of();
        }
    }

    public static Map<String, Range> getTypes(DOMDocument doc) {
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

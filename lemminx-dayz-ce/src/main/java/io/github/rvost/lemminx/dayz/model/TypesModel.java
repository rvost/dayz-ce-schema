package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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
    private static final Path rootPath = Path.of(TypesModel.DB_FOLDER, TypesModel.TYPES_FILE);

    public static boolean isTypes(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && TYPES_TAG.equals(docElement.getNodeName());
    }

    public static boolean isRootTypes(Path missionRoot, Path file){
        var relative = missionRoot.relativize(file);
        return rootPath.equals(relative);
    }

    public static Set<String> getRootTypes(Path missionPath) {
        var path = missionPath.resolve(rootPath);

        try (var input = Files.newInputStream(path)) {
            return getTypes(input);
        } catch (IOException e) {
            return Set.of();
        }
    }

    public static Set<String> getTypes(Path path) {
        try (var input = Files.newInputStream(path)) {
            return getTypes(input);
        } catch (IOException e) {
            return Set.of();
        }
    }

    public static Set<String> getTypes(InputStream input) throws IOException {
        try {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                return getClassnames(doc.getElementsByTagName(TYPE_TAG));
            } else {
                return Set.of();
            }

        } catch (ParserConfigurationException | SAXException e) {
            return Set.of();
        }
    }

    private static Set<String> getClassnames(NodeList nodes) {
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

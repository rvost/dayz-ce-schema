package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Set;

public class TypesModel {
    public static final String TYPES_TAG = "types";
    public static final String USAGE_TAG = "usage";
    public static final String VALUE_TAG = "value";
    public static final String TAG_TAG = "tag";
    public static final String CATEGORY_TAG = "category";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_ATTRIBUTE = "user";
    public static final Set<String> LIMITS_TAGS = Set.of(USAGE_TAG, VALUE_TAG, TAG_TAG, CATEGORY_TAG);
    public static final Set<String> USER_LIMITS_TAGS = Set.of(USAGE_TAG, VALUE_TAG);

    public static boolean isTypes(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && TYPES_TAG.equals(docElement.getNodeName());
    }
}

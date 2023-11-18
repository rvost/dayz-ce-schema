package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

public class TypesModel {
    public static final String TYPES_TAG = "types";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String USER_ATTRIBUTE = "user";

    public static boolean isTypes(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && TYPES_TAG.equals(docElement.getNodeName());
    }
}

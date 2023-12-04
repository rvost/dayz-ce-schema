package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

public class GlobalsModel {
    public static final String GLOBALS_FILE = "globals.xml";
    public static final String VARIABLES_TAG = "variables";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String VALUE_ATTRIBUTE = "value";

    public static boolean isGlobals(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && VARIABLES_TAG.equals(docElement.getNodeName());
    }
}


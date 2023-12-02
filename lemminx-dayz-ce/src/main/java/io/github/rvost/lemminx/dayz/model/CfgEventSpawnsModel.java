package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

public class CfgEventSpawnsModel {
    public static final String CFGEVENTSPAWNS_FILE = "cfgeventspawns.xml";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean isEventSpawns(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(CFGEVENTSPAWNS_FILE);
    }
}

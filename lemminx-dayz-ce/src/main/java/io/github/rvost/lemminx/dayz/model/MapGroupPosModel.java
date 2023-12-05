package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

public class MapGroupPosModel {
    public static final String MAPGROUPPOS_FILE = "mapgrouppos.xml";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean isMapGroupPos(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(MAPGROUPPOS_FILE);
    }
}

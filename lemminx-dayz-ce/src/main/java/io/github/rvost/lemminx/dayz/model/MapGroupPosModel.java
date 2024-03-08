package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMDocument;

public class MapGroupPosModel {
    public static final String MAPGROUPPOS_FILE = "mapgrouppos.xml";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, MAPGROUPPOS_FILE);
    }
}

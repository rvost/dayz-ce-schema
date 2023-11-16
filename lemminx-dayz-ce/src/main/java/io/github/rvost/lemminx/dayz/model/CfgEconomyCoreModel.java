package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

public class CfgEconomyCoreModel {
    public static final String CFGECONOMYCORE_XML = "cfgeconomycore.xml";
    public static final String CE_TAG = "ce";
    public static final String FOLDER_ATTRIBUTE = "folder";
    public static final String FILE_TAG = "file";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean isCfgEconomyCore(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(CFGECONOMYCORE_XML);
    }
}

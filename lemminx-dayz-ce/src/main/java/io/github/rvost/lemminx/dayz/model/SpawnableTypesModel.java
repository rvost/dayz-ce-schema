package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

public class SpawnableTypesModel {
    public static final String SPAWNABLETYPES_TAG = "spawnabletypes";
    public static final String PRESET_ATTRIBUTE = "preset";

    public static boolean isSpawnableTypes(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && SPAWNABLETYPES_TAG.equals(docElement.getNodeName());
    }
}
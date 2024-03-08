package io.github.rvost.lemminx.dayz.utils;

import org.eclipse.lemminx.dom.DOMDocument;

public class DocumentUtils {
    public static boolean filenameMatch(DOMDocument document, String filename) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(filename);
    }

    public static boolean documentTagMatch(DOMDocument document, String tagName) {
        if (document == null) {
            return false;
        }
        var docElement = document.getDocumentElement();
        return docElement != null && tagName.equals(docElement.getNodeName());
    }
}

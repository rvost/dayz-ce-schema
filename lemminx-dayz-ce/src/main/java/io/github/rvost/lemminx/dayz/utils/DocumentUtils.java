package io.github.rvost.lemminx.dayz.utils;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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

    public static Optional<DOMDocument> tryParseDocument(Path path){
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(path));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, path.toString()), null);
            return Optional.ofNullable(doc);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}

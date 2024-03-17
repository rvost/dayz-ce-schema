package io.github.rvost.lemminx.dayz.utils;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static Optional<DOMDocument> tryParseDocument(Path path) {
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(path));
            var uri = path.toUri().toString();
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, uri), null);
            return Optional.ofNullable(doc);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Map<String, Range> indexByAttribute(DOMDocument doc, String attribute) {
        return doc.getDocumentElement().getChildren().stream()
                .filter(n -> n.hasAttribute(attribute))
                .map(n -> n.getAttributeNode(attribute))
                .collect(Collectors.toMap(
                        DOMAttr::getNodeValue,
                        n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                        (oldValue, newValue) -> newValue,
                        HashMap::new));
    }

    public static Map<String, List<Range>> indexChildrenByAttribute(DOMDocument doc, String attribute) {
        return doc.getDocumentElement().getChildren().stream()
                .flatMap(x -> x.getChildren().stream())
                .filter(x -> x.hasAttribute(attribute))
                .collect(Collectors.groupingBy(
                        x -> x.getAttribute(attribute),
                        Collectors.mapping(
                                x -> XMLPositionUtility.selectWholeTag(x.getStart() + 1, doc),
                                Collectors.toList()
                        ))
                );
    }
}

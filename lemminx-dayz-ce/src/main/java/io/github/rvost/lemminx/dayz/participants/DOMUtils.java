package io.github.rvost.lemminx.dayz.participants;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DOMUtils {
    public static List<DOMNode> getSiblings(DOMNode node) {
        var parent = node.getParentNode();
        return parent.getChildren().stream()
                .filter(other -> other != node)
                .toList();
    }

    public static List<DOMNode> getSiblings(DOMNode node, String tagName) {
        var parent = node.getParentNode();
        return parent.getChildren().stream()
                .filter(n -> tagName.equals(n.getNodeName()))
                .filter(other -> other != node)
                .toList();
    }

    public static List<String> getAttributeValues(List<DOMNode> nodes, String attributeName) {
        return nodes.stream()
                .map(node -> node.getAttribute(attributeName))
                .filter(Objects::nonNull)
                .toList();
    }

    public static List<DOMNode> getSiblingsInRange(DOMNode node, String tagName, DOMDocument document, Range selectedRange) {
        try {
            var startOffset = document.offsetAt(selectedRange.getStart());
            var endOffset = document.offsetAt(selectedRange.getEnd());

            return getSiblings(node, tagName).stream()
                    .filter(x -> ParticipantsUtils.inRange(x.getStart(), startOffset, endOffset))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (BadLocationException ex) {
            return List.of();
        }
    }
}

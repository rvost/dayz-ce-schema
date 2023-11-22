package io.github.rvost.lemminx.dayz.participants;

import org.eclipse.lemminx.dom.DOMNode;

import java.util.List;
import java.util.Objects;

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
}

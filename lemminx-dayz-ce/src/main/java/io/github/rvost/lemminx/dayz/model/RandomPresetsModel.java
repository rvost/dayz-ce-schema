package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class RandomPresetsModel {
    public static final String CFGRANDOMPRESETS_FILE = "cfgrandompresets.xml";
    public static final String CARGO_TAG = "cargo";
    public static final String ATTACHMENTS_TAG = "attachments";
    public static final String ITEM_TAG = "item";
    public static final String NAME_ATTRIBUTE = "name";

    public static boolean match(DOMDocument document) {
        return DocumentUtils.filenameMatch(document, CFGRANDOMPRESETS_FILE);
    }

    public static Map<String, Set<String>> getRandomPresets(Path missionPath) {
        var path = missionPath.resolve(CFGRANDOMPRESETS_FILE);

        try (var input = Files.newInputStream(path)) {
            return getRandomPresets(input);
        } catch (IOException e) {
            return Map.of();
        }
    }

    public static Map<String, Set<String>> getRandomPresets(InputStream input) throws IOException {
        try {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                var cargo = getValues(doc.getElementsByTagName(CARGO_TAG));
                var attachments = getValues(doc.getElementsByTagName(ATTACHMENTS_TAG));
                return new HashMap<>(Map.ofEntries(
                        entry(CARGO_TAG, cargo),
                        entry(ATTACHMENTS_TAG, attachments)
                ));
            } else {
                return Map.of();
            }
        } catch (ParserConfigurationException | SAXException e) {
            return Map.of();
        }
    }

    public static Map<String, Range> getRandomPresetsIndex(Path missionPath) {
        var filePath = missionPath.resolve(CFGRANDOMPRESETS_FILE);
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(filePath));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, filePath.toString()), null);
            return doc.getDocumentElement().getChildren().stream()
                    .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                    .map(n -> n.getAttributeNode(NAME_ATTRIBUTE))
                    .collect(Collectors.toMap(
                            DOMAttr::getNodeValue,
                            n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                            (oldValue, newValue) -> oldValue,
                            HashMap::new));
        } catch (IOException e) {
            return Map.of();
        }
    }

    private static Set<String> getValues(NodeList nodes) {
        var result = new HashSet<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);
            var attributes = node.getAttributes();
            if (attributes != null) {
                var nameAttr = attributes.getNamedItem(NAME_ATTRIBUTE);
                if (nameAttr != null) {
                    result.add(nameAttr.getNodeValue());
                }
            }
        }
        return result;
    }
}

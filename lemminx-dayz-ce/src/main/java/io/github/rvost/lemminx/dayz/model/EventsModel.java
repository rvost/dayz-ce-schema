package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EventsModel {
    public static final String DB_FOLDER = "db";
    public static final String EVENTS_FILE = "events.xml";
    public static final String EVENTS_TAG = "events";
    public static final String EVENT_TAG = "event";
    public static final String CHILDREN_TAG = "children";
    public static final String LIFETIME_TAG = "lifetime";
    public static final String RESTOCK_TAG = "restock";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final Set<String> TIME_INTERVAL_TAGS = Set.of(LIFETIME_TAG, RESTOCK_TAG);

    // TODO: Move to resources
    public static final List<String> EVENT_NAME_PREFIXES =
            Arrays.asList("Animal", "Ambient", "Infected", "Item", "Loot", "Static", "Trajectory", "Vehicle");
    public static final List<String> EVENT_SPAWNS_PREFIXES =
            Arrays.asList("Item", "Loot", "Static", "Vehicle");
    public static final Path rootEventsPath = Path.of(DB_FOLDER, EVENTS_FILE);


    public static boolean isEvents(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && EVENTS_TAG.equals(docElement.getNodeName());
    }

    public static boolean isRootEvents(Path missionRoot, Path file) {
        var relative = missionRoot.relativize(file);
        return rootEventsPath.equals(relative);
    }

    public static Map<String, Range> getRootEvents(Path missionPath) {
        var filePath = missionPath.resolve(rootEventsPath);
        return getEvents(filePath);
    }

    public static Map<String, Range> getEvents(Path path) {
        try {
            var fileContent = String.join(System.lineSeparator(), Files.readAllLines(path));
            var doc = DOMParser.getInstance().parse(new TextDocument(fileContent, path.toString()), null);
            return getEvents(doc);
        } catch (IOException e) {
            return Map.of();
        }
    }

    public static Map<String, Range> getEvents(DOMDocument doc) throws IOException {
        return doc.getDocumentElement().getChildren().stream()
                .filter(n -> n.hasAttribute(NAME_ATTRIBUTE))
                .map(n -> n.getAttributeNode(NAME_ATTRIBUTE))
                .collect(Collectors.toMap(
                        DOMAttr::getNodeValue,
                        n -> XMLPositionUtility.selectWholeTag(n.getStart(), doc),
                        (oldValue, newValue) -> newValue,
                        HashMap::new));
    }
}

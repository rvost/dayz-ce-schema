package io.github.rvost.lemminx.dayz.model;

import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Range;

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


    public static boolean match(DOMDocument document) {
        return DocumentUtils.documentTagMatch(document, EVENTS_TAG);
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
        return DocumentUtils.tryParseDocument(path)
                .map(EventsModel::getEvents)
                .orElse(Map.of());
    }

    public static Map<String, Range> getEvents(DOMDocument doc) {
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

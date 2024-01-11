package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            Arrays.asList("Loot", "Static", "Item", "Animal", "Infected", "Trajectory", "Vehicle");
    public static final List<String> EVENT_SPAWNS_PREFIXES =
            Arrays.asList("Item", "Loot", "Static", "Vehicle");
    private static final Path rootPath = Path.of(DB_FOLDER, EVENTS_FILE);


    public static boolean isEvents(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && EVENTS_TAG.equals(docElement.getNodeName());
    }

    public static boolean isRootEvents(Path missionRoot, Path file){
        var relative = missionRoot.relativize(file);
        return rootPath.equals(relative);
    }

    public static Set<String> getRootEvents(Path missionPath) {
        var path = missionPath.resolve(rootPath);

        try (var input = Files.newInputStream(path)) {
            return getEvents(input);
        } catch (IOException e) {
            return Set.of();
        }
    }

    public static Set<String> getEvents(Path path) {
        try (var input = Files.newInputStream(path)) {
            return getEvents(input);
        } catch (IOException e) {
            return Set.of();
        }
    }

    public static Set<String> getEvents(InputStream input) throws IOException {
        try {
            var db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            var doc = db.parse(input);
            if (doc.getDocumentElement() != null) {
                doc.getDocumentElement().normalize();
                return getEventNames(doc.getElementsByTagName(EVENT_TAG));
            } else {
                return Set.of();
            }

        } catch (ParserConfigurationException | SAXException e) {
            return Set.of();
        }
    }

    private static Set<String> getEventNames(NodeList nodes) {
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

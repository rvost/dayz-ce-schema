package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Arrays;
import java.util.List;

public class EventsModel {
    public static final String EVENTS_TAG = "events";
    public static final String CHILDREN_TAG = "children";

    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";

    // TODO: Move to resources
    public static final List<String> EVENT_NAME_PREFIXES =
            Arrays.asList("Loot", "Static", "Item", "Animal", "Infected", "Trajectory", "Vehicle");

    public static boolean isEvents(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && EVENTS_TAG.equals(docElement.getNodeName());
    }
}

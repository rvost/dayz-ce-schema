package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import io.github.rvost.lemminx.dayz.model.EventsModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class EventsReferenceParticipant extends AbstractReferenceParticipant {

    private final DayzMissionService missionService;

    public EventsReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return EventsModel.match(document);
    }

    @Override
    protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext referenceContext,
                                  List<Location> locations, CancelChecker cancelChecker) {
        DOMAttr attr = node.findAttrAt(offset);
        if (attr != null && EventsModel.NAME_ATTRIBUTE.equals(attr.getName())) {
            var event = attr.getValue();
            provideOtherFileReferences(event, node.getOwnerDocument(), locations);
            cancelChecker.checkCanceled();
            provideEventSpawnsReference(event, locations);
        }

    }

    private void provideOtherFileReferences(String event, DOMDocument document, List<Location> locations) {
        var index = missionService.getEventIndex();
        if (index.containsKey(event)) {
            try {
                var docUri = new URI(document.getDocumentURI());
                var options = index.get(event);
                options.stream()
                        .filter(e -> !e.getKey().equals(Path.of(docUri)))
                        .forEach(e -> locations.add(new Location(e.getKey().toUri().toString(), e.getValue())));
            } catch (URISyntaxException ignored) {

            }
        }
    }

    private void provideEventSpawnsReference(String event, List<Location> locations) {
        var index = missionService.getEventSpawns();
        if (index.containsKey(event)) {
            var file = missionService.missionRoot.resolve(CfgEventSpawnsModel.CFGEVENTSPAWNS_FILE)
                    .toUri().toString();
            var range = index.get(event);
            locations.add(new Location(file, range));
        }
    }
}

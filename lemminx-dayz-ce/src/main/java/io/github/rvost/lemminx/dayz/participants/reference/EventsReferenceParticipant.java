package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import io.github.rvost.lemminx.dayz.model.EventsModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class EventsReferenceParticipant extends AbstractReferenceParticipant {

    private final DayzMissionService missionService;

    public EventsReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return EventsModel.isEvents(document);
    }

    @Override
    protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext referenceContext,
                                  List<Location> locations, CancelChecker cancelChecker) {
        DOMAttr attr = node.findAttrAt(offset);
        if (attr != null && EventsModel.NAME_ATTRIBUTE.equals(attr.getName())) {
            var event = attr.getValue();
            var index = missionService.getEventSpawns();
            if (index.containsKey(event)) {
                var file = missionService.missionRoot.resolve(CfgEventSpawnsModel.CFGEVENTSPAWNS_FILE)
                        .toUri().toString();
                var range = index.get(event);
                locations.add(new Location(file, range));
            }
        }

    }
}

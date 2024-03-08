package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.EventsModel;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionUserModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class UserFlagReferenceParticipant extends AbstractReferenceParticipant {
    private final DayzMissionService missionService;

    public UserFlagReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return LimitsDefinitionUserModel.match(document);
    }

    @Override
    protected void findReferences(DOMNode node,
                                  Position position,
                                  int offset,
                                  ReferenceContext referenceContext,
                                  List<Location> locations,
                                  CancelChecker cancelChecker) {
        var nodeName = node.getLocalName();
        if (LimitsDefinitionUserModel.USER_TAG.equals(nodeName)) {
            DOMAttr attr = node.findAttrAt(offset);
            if (attr != null && EventsModel.NAME_ATTRIBUTE.equals(attr.getName())) {
                var flag = attr.getValue();
                provideUserFlagsReferences(flag, locations);
            }
        }
    }

    private void provideUserFlagsReferences(String flag, List<Location> locations) {
        var index = missionService.getUserFlagReferences();
        if (index.containsKey(flag)) {
            var options = index.get(flag);
            options.stream()
                    .map(e -> new Location(e.getKey().toUri().toString(), e.getValue()))
                    .forEach(locations::add);
        }
    }
}

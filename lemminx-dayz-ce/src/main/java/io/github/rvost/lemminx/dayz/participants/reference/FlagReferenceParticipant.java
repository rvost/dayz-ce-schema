package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class FlagReferenceParticipant extends AbstractReferenceParticipant {
    private final DayzMissionService missionService;

    public FlagReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return LimitsDefinitionsModel.match(document);
    }

    @Override
    protected void findReferences(DOMNode node,
                                  Position position,
                                  int offset,
                                  ReferenceContext referenceContext,
                                  List<Location> locations,
                                  CancelChecker cancelChecker) {
        DOMAttr attr = node.findAttrAt(offset);
        if (attr != null && LimitsDefinitionsModel.NAME_ATTRIBUTE.equals(attr.getName())) {
            var flag = attr.getValue();
            provideFlagsReferences(flag, locations);
        }
    }

    private void provideFlagsReferences(String flag, List<Location> locations) {
        var index = missionService.getFlagReferences();
        if (index.containsKey(flag)) {
            var options = index.get(flag);
            options.stream()
                    .map(e -> new Location(e.getKey().toUri().toString(), e.getValue()))
                    .forEach(locations::add);
        }
    }
}

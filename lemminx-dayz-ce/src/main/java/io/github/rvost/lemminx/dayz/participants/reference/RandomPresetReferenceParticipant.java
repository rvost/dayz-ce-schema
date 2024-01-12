package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.EventsModel;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class RandomPresetReferenceParticipant extends AbstractReferenceParticipant {
    private final DayzMissionService missionService;

    public RandomPresetReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return RandomPresetsModel.isRandomPresets(document);
    }

    @Override
    protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext referenceContext,
                                  List<Location> locations, CancelChecker cancelChecker) {
        var nodeName = node.getLocalName();
        if (RandomPresetsModel.CARGO_TAG.equals(nodeName) || RandomPresetsModel.ATTACHMENTS_TAG.equals(nodeName)) {
            DOMAttr attr = node.findAttrAt(offset);
            if (attr != null && EventsModel.NAME_ATTRIBUTE.equals(attr.getName())) {
                var preset = attr.getValue();
                provideSpawnableTypesReferences(preset, locations);
            }
        }
    }

    private void provideSpawnableTypesReferences(String preset, List<Location> locations) {
        var index = missionService.getRandomPresetsReferences();
        if (index.containsKey(preset)) {
            var options = index.get(preset);
            options.stream()
                    .map(e -> new Location(e.getKey().toUri().toString(), e.getValue()))
                    .forEach(locations::add);
        }
    }
}

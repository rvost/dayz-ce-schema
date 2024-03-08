package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventGroupsModel;
import io.github.rvost.lemminx.dayz.model.MapGroupProtoModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class MapGroupProtoReferenceParticipant extends AbstractReferenceParticipant {
    private final DayzMissionService missionService;

    public MapGroupProtoReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return MapGroupProtoModel.match(document);
    }

    @Override
    protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext referenceContext,
                                  List<Location> locations, CancelChecker cancelChecker) {
        if (MapGroupProtoModel.GROUP_TAG.equals(node.getLocalName())) {
            DOMAttr attr = node.findAttrAt(offset);
            if (attr != null && MapGroupProtoModel.NAME_ATTRIBUTE.equals(attr.getName())) {
                var group = attr.getValue();
                provideEventGroupsReference(group, locations);
            }
        }
    }

    private void provideEventGroupsReference(String group, List<Location> locations) {
        var index = CfgEventGroupsModel.getChildTypesIndex(missionService.missionRoot);
        if (index.containsKey(group)) {
            var options = index.get(group);
            var uri = missionService.missionRoot.resolve(CfgEventGroupsModel.CFGEVENTGROUPS_FILE).toUri().toString();
            options.stream()
                    .map(r -> new Location(uri, r))
                    .forEach(locations::add);
        }
    }
}

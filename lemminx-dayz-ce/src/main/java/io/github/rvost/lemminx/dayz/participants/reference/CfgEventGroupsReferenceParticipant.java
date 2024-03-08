package io.github.rvost.lemminx.dayz.participants.reference;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventGroupsModel;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.AbstractReferenceParticipant;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class CfgEventGroupsReferenceParticipant extends AbstractReferenceParticipant {
    private final DayzMissionService missionService;

    public CfgEventGroupsReferenceParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    protected boolean match(DOMDocument document) {
        return CfgEventGroupsModel.match(document);
    }

    @Override
    protected void findReferences(DOMNode node, Position position, int offset, ReferenceContext referenceContext,
                                  List<Location> locations, CancelChecker cancelChecker) {
        if (CfgEventGroupsModel.GROUP_TAG.equals(node.getLocalName())) {
            DOMAttr attr = node.findAttrAt(offset);
            if (attr != null && TypesModel.NAME_ATTRIBUTE.equals(attr.getName())) {
                var group = attr.getValue();
                provideEventSpawnReference(group, locations);
            }
        }
    }

    private void provideEventSpawnReference(String group, List<Location> locations) {
        var index = CfgEventSpawnsModel.getCfgEventSpawnsGroupReferences(missionService.missionRoot);
        if(index.containsKey(group)){
            var range = index.get(group);
            var file = missionService.missionRoot.resolve(CfgEventSpawnsModel.CFGEVENTSPAWNS_FILE);
            locations.add(new Location(file.toUri().toString(), range));
        }
    }
}

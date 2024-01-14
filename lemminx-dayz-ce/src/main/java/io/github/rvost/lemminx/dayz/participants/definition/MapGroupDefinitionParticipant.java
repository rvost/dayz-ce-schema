package io.github.rvost.lemminx.dayz.participants.definition;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventGroupsModel;
import io.github.rvost.lemminx.dayz.model.MapGroupProtoModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.services.extensions.IDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class MapGroupDefinitionParticipant implements IDefinitionParticipant {
    private final DayzMissionService missionService;

    public MapGroupDefinitionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void findDefinition(IDefinitionRequest request, List<LocationLink> locations, CancelChecker cancelChecker) {
        var document = request.getXMLDocument();
        var node = request.getNode();
        if (CfgEventGroupsModel.isCfgEventGroups(document)
                && CfgEventGroupsModel.TYPE_ATTRIBUTE.equals(node.getLocalName())) {
            var group = node.getNodeValue();
            var index = missionService.getMapGroupIndex();
            if (index.containsKey(group)) {
                var file = missionService.missionRoot.resolve(MapGroupProtoModel.MAPGROUPPROTO_FILE);
                var range = index.get(group);
                locations.add(ParticipantsUtils.toDefinitionLocationLink(file, range, node));
            }
        }
    }
}

package io.github.rvost.lemminx.dayz.participants.definition;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.services.extensions.IDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class FlagsDefinitionParticipant implements IDefinitionParticipant {

    private final DayzMissionService missionService;

    public FlagsDefinitionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void findDefinition(IDefinitionRequest request, List<LocationLink> locations, CancelChecker cancelChecker) {
        var document = request.getXMLDocument();
        var node = request.getNode();
        if (TypesModel.isTypes(document) && TypesModel.USER_ATTRIBUTE.equals(node.getLocalName())) {
            var flag = node.getNodeValue();
            var index = missionService.getUserFlagsIndex();
            if (index.containsKey(flag)) {
                var file = missionService.missionRoot.resolve(LimitsDefinitionsModel.USER_LIMITS_DEFINITION_FILE);
                var range = index.get(flag);
                locations.add(ParticipantsUtils.toDefinitionLocationLink(file, range, node));
            }
        }
    }
}

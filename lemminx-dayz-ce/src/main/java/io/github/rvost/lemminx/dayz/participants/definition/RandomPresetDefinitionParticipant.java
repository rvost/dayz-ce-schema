package io.github.rvost.lemminx.dayz.participants.definition;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import io.github.rvost.lemminx.dayz.model.SpawnableTypesModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.services.extensions.IDefinitionParticipant;
import org.eclipse.lemminx.services.extensions.IDefinitionRequest;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

public class RandomPresetDefinitionParticipant implements IDefinitionParticipant {

    private final DayzMissionService missionService;

    public RandomPresetDefinitionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void findDefinition(IDefinitionRequest request, List<LocationLink> locations, CancelChecker cancelChecker) {
        var document = request.getXMLDocument();
        var node = request.getNode();
        if (SpawnableTypesModel.match(document) &&
                SpawnableTypesModel.PRESET_ATTRIBUTE.equals(node.getLocalName())) {
            var preset = node.getNodeValue();
            var index = missionService.getRandomPresetsIndex();
            if(index.containsKey(preset)){
                var file = missionService.missionRoot.resolve(RandomPresetsModel.CFGRANDOMPRESETS_FILE);
                var range = index.get(preset);
                locations.add(ParticipantsUtils.toDefinitionLocationLink(file, range, node));
            }
        }
    }
}

package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.SpawnableTypesModel;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class SpawnableTypesCompletionParticipant extends CompletionParticipantAdapter {

    private final DayzMissionService missionService;

    public SpawnableTypesCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (SpawnableTypesModel.isSpawnableTypes(doc)) {
            computeSpawnableTypesCompletion(request, response, doc);
        }
    }

    private void computeSpawnableTypesCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);

        if (SpawnableTypesModel.PRESET_ATTRIBUTE.equals(attr.getName())) {
            var availablePresets = missionService.getRandomPresets();
            if (availablePresets.containsKey(node.getNodeName())) {
                availablePresets.get(node.getNodeName()).stream()
                        .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                        .forEach(response::addCompletionItem);
            }
        }
        
        if (SpawnableTypesModel.NAME_ATTRIBUTE.equals(attr.getName())){
            var availableTypes = missionService.getRootTypes();
            availableTypes.stream()
                    .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Class))
                    .forEach(response::addCompletionItem);
        }
    }
}

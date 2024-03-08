package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.MapGroupProtoModel;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import io.github.rvost.lemminx.dayz.participants.DOMUtils;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.HashSet;

public class MapGroupProtoCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public MapGroupProtoCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (MapGroupProtoModel.match(doc)) {
            computeCompletion(request, response, doc);
        }
    }

    private void computeCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);

        var limitsDefinitions = missionService.getLimitsDefinitions();
        var userLimitsDefinitions = missionService.getUserLimitsDefinitions();

        if (MapGroupProtoModel.NAME_ATTRIBUTE.equals(attr.getName()) && limitsDefinitions.containsKey(node.getNodeName())) {
            var options = limitsDefinitions.get(node.getNodeName());
            var siblingNodes = DOMUtils.getSiblings(node, node.getNodeName());
            var exclusions = new HashSet<String>();

            if (!siblingNodes.isEmpty()) {
                var siblingValues = DOMUtils.getAttributeValues(siblingNodes, TypesModel.NAME_ATTRIBUTE);
                exclusions.addAll(siblingValues);
            }

            options.stream()
                    .filter(option -> !exclusions.contains(option))
                    .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                    .forEach(response::addCompletionItem);
        }

        if (MapGroupProtoModel.USER_ATTRIBUTE.equals(attr.getName()) && userLimitsDefinitions.containsKey(node.getNodeName())) {
            var options = userLimitsDefinitions.get(node.getNodeName());

            options.stream()
                    .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                    .forEach(response::addCompletionItem);
        }
    }
}

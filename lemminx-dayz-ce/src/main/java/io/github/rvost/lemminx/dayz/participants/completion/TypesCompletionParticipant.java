package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.participants.DOMUtils;
import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.HashSet;

public class TypesCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public TypesCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (TypesModel.isTypes(doc)) {
            computeTypesCompletion(request, response, doc);
        }
    }

    private void computeTypesCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);
        var availableDefinitions = missionService.getLimitsDefinitions();
        var availableUserDefinitions = missionService.getUserLimitsDefinitions();

        if (TypesModel.NAME_ATTRIBUTE.equals(attr.getName()) && availableDefinitions.containsKey(node.getNodeName())) {
            var siblingNodes = DOMUtils.getSiblings(node, node.getNodeName());
            var exclusions = new HashSet<String>();
            if (!siblingNodes.isEmpty()) {
                var siblingValues = DOMUtils.getAttributeValues(siblingNodes, TypesModel.NAME_ATTRIBUTE);
                exclusions.addAll(siblingValues);
            }

            availableDefinitions.get(node.getNodeName()).stream()
                    .filter(option -> !exclusions.contains(option))
                    .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                    .forEach(response::addCompletionItem);
        }
        if (TypesModel.USER_ATTRIBUTE.equals(attr.getName()) && availableUserDefinitions.containsKey(node.getNodeName())) {
            availableUserDefinitions.get(node.getNodeName()).stream()
                    .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                    .forEach(response::addCompletionItem);
        }
    }
}

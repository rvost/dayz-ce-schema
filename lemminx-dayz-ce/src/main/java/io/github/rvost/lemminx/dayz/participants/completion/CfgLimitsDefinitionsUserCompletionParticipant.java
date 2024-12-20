package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.model.LimitsDefinitionUserModel;
import io.github.rvost.lemminx.dayz.participants.DOMUtils;
import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.HashSet;

public class CfgLimitsDefinitionsUserCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public CfgLimitsDefinitionsUserCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (LimitsDefinitionUserModel.match(doc)) {
            computeUserLimitsDefinitionsCompletion(request, response, doc);
        }

    }

    private void computeUserLimitsDefinitionsCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);

        if (LimitsDefinitionModel.NAME_ATTRIBUTE.equals(attr.getName())) {
            var availableDefinitions = missionService.getLimitsDefinitions();
            if (availableDefinitions.containsKey(node.getNodeName())) {
                var siblingNodes = DOMUtils.getSiblings(node);
                var exclusions = new HashSet<String>();
                if (!siblingNodes.isEmpty()) {
                    var siblingValues = DOMUtils.getAttributeValues(siblingNodes, LimitsDefinitionModel.NAME_ATTRIBUTE);
                    exclusions.addAll(siblingValues);
                }
                availableDefinitions.get(node.getNodeName()).stream()
                        .filter(option -> !exclusions.contains(option))
                        .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                        .forEach(response::addCompletionItem);
            }
        }
    }
}

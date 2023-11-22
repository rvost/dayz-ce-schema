package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class CfgLimitsDefinitionsUserCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public CfgLimitsDefinitionsUserCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (LimitsDefinitionsModel.isUserLimitsDefinitions(doc)) {
            computeUserLimitsDefinitionsCompletion(request, response, doc);
        }

    }

    private void computeUserLimitsDefinitionsCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);

        if (LimitsDefinitionsModel.NAME_ATTRIBUTE.equals(attr.getName())) {
            var availableDefinitions = missionService.getLimitsDefinitions();
            if (availableDefinitions.containsKey(node.getNodeName())) {
                var siblingNodes = getSiblings(node);
                var exclusions = new HashSet<String>();
                if (!siblingNodes.isEmpty()) {
                    var siblingValues = getAttributeValues(siblingNodes, LimitsDefinitionsModel.NAME_ATTRIBUTE);
                    exclusions.addAll(siblingValues);
                }
                availableDefinitions.get(node.getNodeName()).stream()
                        .filter(option -> !exclusions.contains(option))
                        .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                        .forEach(response::addCompletionItem);
            }
        }
    }

    private List<DOMNode> getSiblings(DOMNode node) {
        var parent = node.getParentNode();
        return parent.getChildren().stream()
                .filter(other -> other != node)
                .toList();
    }

    private List<String> getAttributeValues(List<DOMNode> nodes, String attributeName) {
        return nodes.stream()
                .map(node -> node.getAttribute(attributeName))
                .filter(Objects::nonNull)
                .toList();
    }
}

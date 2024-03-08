package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEnvironmentModel;
import io.github.rvost.lemminx.dayz.participants.DOMUtils;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.HashSet;
import java.util.Optional;

public class CfgEnvironmentCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public CfgEnvironmentCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (CfgEnvironmentModel.match(doc)) {
            computeCfgEnvironmentCompletion(request, response, doc);
        }
    }

    private void computeCfgEnvironmentCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);

        if (CfgEnvironmentModel.PATH_ATTRIBUTE.equals(attr.getName())) {
            var path = missionService.getEnvFiles();
            path.stream()
                    .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.File))
                    .forEach(response::addCompletionItem);
        }

        if (CfgEnvironmentModel.USABLE_ATTRIBUTE.equals(attr.getName())) {
            var path = missionService.getEnvFiles();
            var siblingNodes = DOMUtils.getSiblings(node);
            var exclusions = new HashSet<String>();
            if (!siblingNodes.isEmpty()) {
                var siblingValues = DOMUtils.getAttributeValues(siblingNodes, CfgEnvironmentModel.USABLE_ATTRIBUTE);
                exclusions.addAll(siblingValues);
            }

            path.stream()
                    .map(CfgEnvironmentModel::getUsableKeyFromPath)
                    .flatMap(Optional::stream)
                    .filter(option -> !exclusions.contains(option))
                    .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Enum))
                    .forEach(response::addCompletionItem);
        }

    }
}

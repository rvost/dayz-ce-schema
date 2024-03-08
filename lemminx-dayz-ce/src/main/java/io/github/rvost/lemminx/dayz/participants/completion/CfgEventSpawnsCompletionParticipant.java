package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class CfgEventSpawnsCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public CfgEventSpawnsCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (CfgEventSpawnsModel.match(doc)) {
            computeEventSpawnsCompletion(request, response, doc);
        }
    }

    private void computeEventSpawnsCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);

        if (CfgEventSpawnsModel.NAME_ATTRIBUTE.equals(attr.getName())) {
            var available = missionService.getAllEvents();
            available.map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Class))
                    .forEach(response::addCompletionItem);
        }
    }
}

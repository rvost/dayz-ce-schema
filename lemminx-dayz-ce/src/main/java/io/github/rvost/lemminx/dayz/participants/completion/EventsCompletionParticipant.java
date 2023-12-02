package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.EventsModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class EventsCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public EventsCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (EventsModel.isEvents(doc)) {
            computeEventsCompletion(request, response, doc);
        }
    }

    private void computeEventsCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);

        if (EventsModel.TYPE_ATTRIBUTE.equals(attr.getName())) {
            var availableTypes = missionService.getAllTypes();
            availableTypes.map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Class))
                    .forEach(response::addCompletionItem);
        }
    }
}

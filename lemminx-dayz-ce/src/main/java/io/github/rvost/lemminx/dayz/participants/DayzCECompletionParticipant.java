package io.github.rvost.lemminx.dayz.participants;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import static io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel.*;

public class DayzCECompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public DayzCECompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        if (!isCfgEconomyCore(request.getXMLDocument())) {
            return;
        }

        computeValueCompletionResponses(request, response, request.getXMLDocument());
    }

    private void computeValueCompletionResponses(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        int offset = document.offsetAt(editRange.getStart());

        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);
        switch (node.getNodeName()) {
            case CE_TAG -> {
                if (FOLDER_ATTRIBUTE.equals(attr.getName())) {
                    for (var folder : missionService.folders()) {
                        var item = new CompletionItem();
                        var insertText = request.getInsertAttrValue(folder);
                        item.setLabel(insertText);
                        item.setFilterText(insertText);
                        item.setKind(CompletionItemKind.Folder);
                        item.setTextEdit(Either.forLeft(new TextEdit(editRange, insertText)));
                        response.addCompletionItem(item);
                    }

                }
            }
            case FILE_TAG -> {
                if (NAME_ATTRIBUTE.equals(attr.getName())) {
                    var folder = node.getParentNode().getAttribute(FOLDER_ATTRIBUTE);
                    for (var file : missionService.files(folder)) {
                        var item = new CompletionItem();
                        var insertText = request.getInsertAttrValue(file);
                        item.setLabel(insertText);
                        item.setFilterText(insertText);
                        item.setKind(CompletionItemKind.File);
                        item.setTextEdit(Either.forLeft(new TextEdit(editRange, insertText)));
                        response.addCompletionItem(item);
                    }
                }
            }
        }
    }

}

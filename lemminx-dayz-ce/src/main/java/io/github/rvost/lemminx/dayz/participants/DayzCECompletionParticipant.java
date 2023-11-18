package io.github.rvost.lemminx.dayz.participants;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class DayzCECompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public DayzCECompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (CfgEconomyCoreModel.isCfgEconomyCore(doc)) {
            computeCfgEconomyCoreCompletion(request, response, doc);
        } else if (TypesModel.isTypes(doc)) {
            computeTypesCompletion(request, response, doc);
        }

    }

    private void computeCfgEconomyCoreCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());

        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);
        switch (node.getNodeName()) {
            case CfgEconomyCoreModel.CE_TAG -> {
                if (CfgEconomyCoreModel.FOLDER_ATTRIBUTE.equals(attr.getName())) {
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
            case CfgEconomyCoreModel.FILE_TAG -> {
                if (CfgEconomyCoreModel.NAME_ATTRIBUTE.equals(attr.getName())) {
                    var folder = node.getParentNode().getAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE);
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

    private void computeTypesCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);
        var availableDefinitions = missionService.getLimitsDefinitions();

        if (availableDefinitions.containsKey(node.getNodeName()) && TypesModel.NAME_ATTRIBUTE.equals(attr.getName())) {
            var options = availableDefinitions.get(node.getNodeName());
            for (var option : options) {
                var item = new CompletionItem();
                var insertText = request.getInsertAttrValue(option);
                item.setLabel(insertText);
                item.setFilterText(insertText);
                item.setKind(CompletionItemKind.Enum);
                item.setTextEdit(Either.forLeft(new TextEdit(editRange, insertText)));
                response.addCompletionItem(item);
            }
        }
    }
}

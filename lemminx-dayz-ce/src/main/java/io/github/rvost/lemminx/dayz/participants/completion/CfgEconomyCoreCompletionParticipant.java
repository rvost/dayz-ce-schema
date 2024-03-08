package io.github.rvost.lemminx.dayz.participants.completion;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.completion.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.completion.ICompletionResponse;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class CfgEconomyCoreCompletionParticipant extends CompletionParticipantAdapter {
    private final DayzMissionService missionService;

    public CfgEconomyCoreCompletionParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response, CancelChecker cancelChecker) throws Exception {
        var doc = request.getXMLDocument();

        if (CfgEconomyCoreModel.match(doc)) {
            computeCfgEconomyCoreCompletion(request, response, doc);
        }
    }

    private void computeCfgEconomyCoreCompletion(ICompletionRequest request, ICompletionResponse response, DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        var offset = document.offsetAt(editRange.getStart());
        var node = document.findNodeAt(offset);
        var attr = node.findAttrAt(offset);
        var missionFolders = missionService.getMissionFolders();
        switch (node.getNodeName()) {
            case CfgEconomyCoreModel.CE_TAG -> {
                if (CfgEconomyCoreModel.FOLDER_ATTRIBUTE.equals(attr.getName())) {
                    missionFolders.keySet().stream()
                            .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Folder))
                            .forEach(response::addCompletionItem);
                }
            }
            case CfgEconomyCoreModel.FILE_TAG -> {
                if (CfgEconomyCoreModel.NAME_ATTRIBUTE.equals(attr.getName())) {
                    var folder = node.getParentNode().getAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE);
                    missionFolders.get(folder).stream()
                            .map(option -> CompletionUtils.toCompletionItem(option, request, editRange, CompletionItemKind.Folder))
                            .forEach(response::addCompletionItem);
                }
            }
        }
    }

}

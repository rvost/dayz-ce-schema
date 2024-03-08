package io.github.rvost.lemminx.dayz.participants.rename;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventGroupsModel;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.IPositionRequest;
import org.eclipse.lemminx.services.extensions.rename.IPrepareRenameRequest;
import org.eclipse.lemminx.services.extensions.rename.IRenameParticipant;
import org.eclipse.lemminx.services.extensions.rename.IRenameRequest;
import org.eclipse.lemminx.services.extensions.rename.IRenameResponse;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventGroupRenameParticipant implements IRenameParticipant {
    private final DayzMissionService missionService;

    public EventGroupRenameParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public Either<Range, PrepareRenameResult> prepareRename(IPrepareRenameRequest request, CancelChecker cancelChecker) throws CancellationException {
        if (!isMatchingRequest(request)) {
            return null;
        }
        var attr = request.getCurrentAttribute();
        var range = XMLPositionUtility.selectAttributeValue(attr, true);
        return Either.forLeft(range);
    }

    @Override
    public void doRename(IRenameRequest request, IRenameResponse response, CancelChecker cancelChecker) throws CancellationException {
        if (!isMatchingRequest(request)) {
            return;
        }
        var edits = getRenameDocumentEdits(request, cancelChecker);
        cancelChecker.checkCanceled();
        edits.forEach(response::addTextDocumentEdit);
    }

    private List<TextDocumentEdit> getRenameDocumentEdits(IRenameRequest request, CancelChecker cancelChecker) {
        var document = request.getXMLDocument();
        var newValue = request.getNewText();
        var attr = request.getCurrentAttribute();
        var oldValue = attr.getValue();

        var index = getIndex(document, oldValue);
        cancelChecker.checkCanceled();

        var result = index.entrySet().stream()
                .map(e -> {
                    var edits = ParticipantsUtils.toReplaceEdits(newValue, e.getValue());
                    return TextEditUtils.creatTextDocumentEdit(e.getKey(), edits);
                })
                .collect(Collectors.toCollection(ArrayList::new));
        cancelChecker.checkCanceled();

        var te = new TextEdit(XMLPositionUtility.selectAttributeValue(attr, true), newValue);
        result.add(TextEditUtils.creatTextDocumentEdit(document, List.of(te)));

        return result;
    }

    private Map<DOMDocument, List<Range>> getIndex(DOMDocument document, String group) {
        var eventSpawns = missionService.missionRoot.resolve(CfgEventSpawnsModel.CFGEVENTSPAWNS_FILE);
        return Stream.of(eventSpawns)
                .map(path -> path.toUri().toString())
                .map(uri -> DOMUtils.loadDocument(uri, document.getResolverExtensionManager()))
                .map(doc -> {
                    var ranges = findReferencedGroupRanges(doc, group);
                    return Map.entry(doc, ranges);
                })
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<Range> findReferencedGroupRanges(DOMDocument document, String group) {
        return document.getDocumentElement().getChildren().stream()
                .flatMap(n -> n.getChildren().stream())
                .filter(n -> n.hasAttribute(CfgEventSpawnsModel.GROUP_ATTRIBUTE))
                .map(n -> n.getAttributeNode(CfgEventSpawnsModel.GROUP_ATTRIBUTE))
                .filter(attr -> group.equals(attr.getValue()))
                .map(attr -> XMLPositionUtility.selectAttributeValue(attr, true))
                .toList();
    }

    private static boolean isMatchingRequest(IPositionRequest request) {
        var document = request.getXMLDocument();
        if (!CfgEventGroupsModel.match(document)) {
            return false;
        }
        var node = document.findNodeAt(request.getOffset());
        var attr = request.getCurrentAttribute();
        if (attr == null || node == null) {
            return false;
        }

        return CfgEventGroupsModel.GROUP_TAG.equals(node.getLocalName())
                && CfgEventGroupsModel.NAME_ATTRIBUTE.equals(attr.getName());
    }
}

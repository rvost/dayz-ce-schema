package io.github.rvost.lemminx.dayz.participants.rename;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEventSpawnsModel;
import io.github.rvost.lemminx.dayz.model.EventsModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.XMLTextDocumentService;
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
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventsRenameParticipant implements IRenameParticipant {
    private final DayzMissionService missionService;

    public EventsRenameParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public Either<Range, PrepareRenameResult> prepareRename(IPrepareRenameRequest request, CancelChecker cancelChecker) throws CancellationException {
        if (!isMatchingRequest(request)) {
            return null;
        }
        var attr = request.getCurrentAttribute();
        var value = attr.getValue();
        var prefixMatch = EventsModel.EVENT_NAME_PREFIXES.stream()
                .filter(value::startsWith)
                .findFirst();
        var range = XMLPositionUtility.selectAttributeValue(attr, true);
        if (prefixMatch.isPresent()) {
            var prefix = prefixMatch.get();
            var start = range.getStart();
            start.setCharacter(start.getCharacter() + prefix.length());
        }
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

        var prefixMatch = EventsModel.EVENT_NAME_PREFIXES.stream()
                .filter(oldValue::startsWith)
                .findFirst();
        var insertValue = prefixMatch.orElse("") + newValue;

        var eventsIndex = getIndex(document, oldValue);
        cancelChecker.checkCanceled();

        return eventsIndex.entrySet().stream()
                .map(e -> {
                    var edits = ParticipantsUtils.toReplaceEdits(insertValue, e.getValue());
                    return TextEditUtils.creatTextDocumentEdit(e.getKey(), edits);
                })
                .toList();
    }


    private Map<DOMDocument, List<Range>> getIndex(DOMDocument document, String name) {
        var events = missionService.getEventsFiles().stream();
        var spawns = Stream.of(missionService.missionRoot.resolve(CfgEventSpawnsModel.CFGEVENTSPAWNS_FILE));

        return Stream.concat(events, spawns)
                .map(path -> path.toUri().toString())
                .map(uri -> DOMUtils.loadDocument(uri, document.getResolverExtensionManager()))
                .map(doc -> {
                    var ranges = findEventRanges(doc, name);
                    return Map.entry(doc, ranges);
                })
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static List<Range> findEventRanges(DOMDocument document, String name) {
        return document.getDocumentElement().getChildren().stream()
                .filter(n -> n.hasAttribute(EventsModel.NAME_ATTRIBUTE))
                .map(n -> n.getAttributeNode(EventsModel.NAME_ATTRIBUTE))
                .filter(attr -> name.equals(attr.getValue()))
                .map(attr -> XMLPositionUtility.selectAttributeValue(attr, true))
                .toList();
    }

    private static boolean isMatchingRequest(IPositionRequest request) {
        var document = request.getXMLDocument();
        if (!EventsModel.isEvents(document)) {
            return false;
        }
        var node = document.findNodeAt(request.getOffset());
        var attr = request.getCurrentAttribute();
        if (attr == null || node == null) {
            return false;
        }

        return EventsModel.EVENT_TAG.equals(node.getLocalName())
                && EventsModel.NAME_ATTRIBUTE.equals(attr.getName());
    }
}

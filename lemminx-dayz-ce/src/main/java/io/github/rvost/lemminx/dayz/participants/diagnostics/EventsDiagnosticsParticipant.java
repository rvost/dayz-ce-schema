package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.EventsModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class EventsDiagnosticsParticipant implements IDiagnosticsParticipant {
    private final DayzMissionService missionService;
    private static final String INVALID_EVENT_NAME_CODE = "invalid_event_name";
    private static final String INVALID_EVENT_NAME_MESSAGE = "Event name \"%s\" has invalid prefix.\n" +
            "The event name must begin with the following: " + EventsModel.EVENT_NAME_PREFIXES.stream()
            .map(s -> "\"" + s + "\"")
            .collect(Collectors.joining(", "));

    public EventsDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (EventsModel.isEvents(domDocument)) {
            validateEventNames(domDocument, list, cancelChecker);
            if(missionService.isInMissionFolder(domDocument)){
                validateTypeReferences(domDocument, list, cancelChecker);
            }
        }
    }

    private void validateEventNames(DOMDocument document, List<Diagnostic> diagnostics, CancelChecker cancelChecker) {
        for (var eventNode : document.getDocumentElement().getChildren()) {
            if (eventNode.hasAttribute(EventsModel.NAME_ATTRIBUTE)) {
                var nameAttr = eventNode.getAttributeNode(EventsModel.NAME_ATTRIBUTE);
                var value = nameAttr.getValue();
                if (EventsModel.EVENT_NAME_PREFIXES.stream().noneMatch(value::startsWith)) {
                    var attrValueRange = nameAttr.getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValueRange);
                    var message = String.format(INVALID_EVENT_NAME_MESSAGE, value);
                    diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_EVENT_NAME_CODE));
                }
            }
            cancelChecker.checkCanceled();
        }
    }

    private void validateTypeReferences(DOMDocument document, List<Diagnostic> diagnostics, CancelChecker cancelChecker){
        for (var eventNode : document.getDocumentElement().getChildren()) {
            eventNode.getChildren().stream()
                    .filter(n -> EventsModel.CHILDREN_TAG.equals(n.getNodeName()))
                    .flatMap(n -> n.getChildren().stream())
                    .map(n -> n.getAttributeNode(EventsModel.TYPE_ATTRIBUTE))
                    .filter(Objects::nonNull)
                    .filter(attr -> !missionService.hasType(attr.getValue()))
                    .map(attr -> {
                        var attrValueRange = attr.getNodeAttrValue();
                        var range = XMLPositionUtility.createRange(attrValueRange);
                        var message = String.format(DiagnosticsUtils.UNRECOGNISED_TYPE_MESSAGE, attr.getValue());
                        return new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, DiagnosticsUtils.UNRECOGNISED_TYPE_CODE);
                    })
                    .forEach(diagnostics::add);
            cancelChecker.checkCanceled();
        }
    }
}

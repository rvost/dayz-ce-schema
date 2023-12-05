package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.MapGroupProtoModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.*;
import java.util.stream.Stream;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class MapGroupProtoDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_LIMIT_DEFINITION_CODE = "invalid_limit_definition";
    private static final String INVALID_LIMIT_DEFINITION_MESSAGE = "%s \"%s\" does not exist.";
    private static final String INVALID_USER_LIMIT_DEFINITION_CODE = "invalid_user_limit_definition";
    private static final String INVALID_USER_LIMIT_DEFINITION_MESSAGE = "User %s flag \"%s\" does not exist.";
    private static final String INVALID_EVENT_REFERENCE_CODE = "invalid_event_reference";
    private static final String INVALID_EVENT_REFERENCE_MESSAGE = "Event \"%s\" does not exist.";
    private final DayzMissionService missionService;

    public MapGroupProtoDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (MapGroupProtoModel.isMapGroupProto(domDocument)) {
            validate(domDocument, list, cancelChecker);
        }
    }

    private void validate(DOMDocument document, List<Diagnostic> diagnostics, CancelChecker cancelChecker) {
        var limitsDefinitions = missionService.getLimitsDefinitions();
        var userLimitDefinitions = missionService.getUserLimitsDefinitions();

        var defaultsNode = document.getDocumentElement().getChildren().getFirst();
        defaultsNode.getChildren().stream()
                .filter(n -> MapGroupProtoModel.DEFAULT_TAG.equals(n.getNodeName()))
                .map(n -> n.getAttributeNode(MapGroupProtoModel.DE_ATTRIBUTE))
                .filter(Objects::nonNull)
                .filter(attr -> !missionService.hasEvent(attr.getValue()))
                .map(attr -> {
                    var attrValueRange = attr.getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValueRange);
                    var message = String.format(INVALID_EVENT_REFERENCE_MESSAGE, attr.getValue());
                    return new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, INVALID_EVENT_REFERENCE_CODE);
                })
                .forEach(diagnostics::add);

        document.getDocumentElement().getChildren().stream()
                .filter(node -> MapGroupProtoModel.GROUP_TAG.equals(node.getNodeName()))
                .flatMap(node -> validateGroup(node, limitsDefinitions, userLimitDefinitions))
                .forEach(diagnostics::add);
    }

    private static Stream<Diagnostic> validateGroup(DOMNode groupNode, Map<String, Set<String>> limitsDefinitions,
                                                    Map<String, Set<String>> userLimitDefinitions) {
        var groupDiagnostics = groupNode.getChildren().stream()
                .filter(node -> limitsDefinitions.containsKey(node.getNodeName()))
                .map(node -> validateGroupFlags(node, limitsDefinitions, userLimitDefinitions))
                .flatMap(Optional::stream);

        var containerDiagnostics = groupNode.getChildren().stream()
                .filter(node -> MapGroupProtoModel.CONTAINER_TAG.equals(node.getNodeName()))
                .flatMap(node -> validateContainer(node, limitsDefinitions));

        return Stream.concat(groupDiagnostics, containerDiagnostics);
    }

    // TODO: Refactor
    private static Optional<Diagnostic> validateGroupFlags(DOMNode flagNode, Map<String, Set<String>> limitsDefinitions,
                                                           Map<String, Set<String>> userLimitDefinitions) {
        var nodeName = flagNode.getNodeName();

        if (flagNode.hasAttribute(MapGroupProtoModel.NAME_ATTRIBUTE)) {
            var attr = flagNode.getAttributeNode(MapGroupProtoModel.NAME_ATTRIBUTE);
            var attrValueRange = attr.getNodeAttrValue();
            var range = XMLPositionUtility.createRange(attrValueRange);

            if (!limitsDefinitions.get(nodeName).contains(attr.getValue())) {
                String message = String.format(INVALID_LIMIT_DEFINITION_MESSAGE, nodeName, attr.getValue());
                return Optional.of(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_LIMIT_DEFINITION_CODE));
            }
        }
        if (flagNode.hasAttribute(MapGroupProtoModel.USER_ATTRIBUTE)) {
            var attr = flagNode.getAttributeNode(MapGroupProtoModel.USER_ATTRIBUTE);
            var attrValueRange = attr.getNodeAttrValue();
            var range = XMLPositionUtility.createRange(attrValueRange);

            if (userLimitDefinitions.containsKey(nodeName) && !userLimitDefinitions.get(nodeName).contains(attr.getValue())) {
                String message = String.format(INVALID_USER_LIMIT_DEFINITION_MESSAGE, nodeName, attr.getValue());
                return Optional.of(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_USER_LIMIT_DEFINITION_CODE));
            }
        }
        return Optional.empty();
    }

    private static Stream<Diagnostic> validateContainer(DOMNode node, Map<String, Set<String>> limitsDefinitions) {
        var result = new ArrayList<Diagnostic>();

        for (var child : node.getChildren()) {
            var nodeName = child.getNodeName();
            if (limitsDefinitions.containsKey(nodeName) && child.hasAttribute(MapGroupProtoModel.NAME_ATTRIBUTE)) {
                var attr = child.getAttributeNode(MapGroupProtoModel.NAME_ATTRIBUTE);
                var attrValueRange = attr.getNodeAttrValue();
                var range = XMLPositionUtility.createRange(attrValueRange);

                if (!limitsDefinitions.get(nodeName).contains(attr.getValue())) {
                    String message = String.format(INVALID_LIMIT_DEFINITION_MESSAGE, nodeName, attr.getValue());
                    result.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_LIMIT_DEFINITION_CODE));
                }
            }
        }
        return result.stream();
    }
}

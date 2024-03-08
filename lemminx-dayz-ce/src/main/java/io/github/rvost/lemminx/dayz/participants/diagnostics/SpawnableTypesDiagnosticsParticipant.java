package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.SpawnableTypesModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class SpawnableTypesDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_RANDOM_PRESET_CODE = "invalid_random_preset";
    private static final String INVALID_RANDOM_PRESET_MESSAGE = "%s preset \"%s\" does not exist.";
    private static final String CONFIGURATION_NOT_ALLOWED_CODE = "configuration_not_allowed";
    private static final String CONFIGURATION_NOT_ALLOWED_MESSAGE = "Configuration not allowed when preset is specified.";
    private static final String ATTRIBUTE_NOT_ALLOWED_CODE = "attribute_not_allowed";
    private static final String ATTRIBUTE_NOT_ALLOWED_MESSAGE = "Attribute not allowed when preset is specified.";

    private final DayzMissionService missionService;

    public SpawnableTypesDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (SpawnableTypesModel.match(domDocument) && missionService.isInMissionFolder(domDocument)) {
            validateSpawnableTypes(domDocument, list);
        }
    }

    private void validateSpawnableTypes(DOMDocument document, List<Diagnostic> diagnostics) {
        var randomPresets = missionService.getRandomPresets();
        for (var typeNode : document.getDocumentElement().getChildren()) {
            if (typeNode.hasAttribute(SpawnableTypesModel.NAME_ATTRIBUTE)) {
                var attr = typeNode.getAttributeNode(SpawnableTypesModel.NAME_ATTRIBUTE);
                if (!missionService.hasType(attr.getValue())) {
                    var attrValue = attr.getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValue);
                    String message = String.format(DiagnosticsUtils.UNRECOGNISED_TYPE_MESSAGE, attr.getValue());
                    diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, DiagnosticsUtils.UNRECOGNISED_TYPE_CODE));
                }
            }
            if (typeNode.hasChildNodes()) {
                for (var node : typeNode.getChildren()) {
                    var kind = node.getNodeName();
                    if (node.hasAttribute(SpawnableTypesModel.PRESET_ATTRIBUTE)) {
                        if (randomPresets.containsKey(kind)) {
                            var attr = node.getAttributeNode(SpawnableTypesModel.PRESET_ATTRIBUTE);
                            if (!randomPresets.get(kind).contains(attr.getValue())) {
                                var attrValue = attr.getNodeAttrValue();
                                var range = XMLPositionUtility.createRange(attrValue);
                                String message = String.format(INVALID_RANDOM_PRESET_MESSAGE, kind, attr.getValue());
                                diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_RANDOM_PRESET_CODE));
                            }
                        }
                        if (node.hasChildNodes()) {
                            var range = XMLPositionUtility.createRange(node.getStart(), node.getEnd(), node.getOwnerDocument());
                            diagnostics.add(new Diagnostic(range, CONFIGURATION_NOT_ALLOWED_MESSAGE, DiagnosticSeverity.Error, ERROR_SOURCE, CONFIGURATION_NOT_ALLOWED_CODE));
                        }
                        node.getAttributeNodes().stream()
                                .filter(n -> !SpawnableTypesModel.PRESET_ATTRIBUTE.equals(n.getName()))
                                .map(n -> {
                                    var range = XMLPositionUtility.createRange(n);
                                    return new Diagnostic(range, ATTRIBUTE_NOT_ALLOWED_MESSAGE, DiagnosticSeverity.Error, ERROR_SOURCE, ATTRIBUTE_NOT_ALLOWED_CODE);
                                })
                                .forEach(diagnostics::add);
                    }
                    for (var itemNode : node.getChildren()) {
                        if (itemNode.hasAttribute(SpawnableTypesModel.NAME_ATTRIBUTE)) {
                            var attr = itemNode.getAttributeNode(SpawnableTypesModel.NAME_ATTRIBUTE);
                            if (!missionService.hasType(attr.getValue())) {
                                var attrValue = attr.getNodeAttrValue();
                                var range = XMLPositionUtility.createRange(attrValue);
                                String message = String.format(DiagnosticsUtils.UNRECOGNISED_TYPE_MESSAGE, attr.getValue());
                                diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, DiagnosticsUtils.UNRECOGNISED_TYPE_CODE));
                            }
                        }
                    }

                }
            }
        }
    }
}

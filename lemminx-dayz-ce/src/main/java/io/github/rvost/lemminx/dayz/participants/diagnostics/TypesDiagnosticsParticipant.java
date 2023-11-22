package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class TypesDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_LIMIT_DEFINITION_CODE = "invalid_limit_definition";
    private static final String INVALID_LIMIT_DEFINITION_MESSAGE = "%s \"%s\" does not exist.";
    private static final String INVALID_USER_LIMIT_DEFINITION_CODE = "invalid_limit_definition";
    private static final String INVALID_USER_LIMIT_DEFINITION_MESSAGE = "User %s flag \"%s\" does not exist.";

    private final DayzMissionService missionService;

    public TypesDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (TypesModel.isTypes(domDocument)) {
            validateTypes(domDocument, list);
        }
    }

    private void validateTypes(DOMDocument document, List<Diagnostic> diagnostics) {
        var limitsDefinitions = missionService.getLimitsDefinitions();
        var userLimitsDefinitions = missionService.getUserLimitsDefinitions();

        for (var typeNode : document.getDocumentElement().getChildren()) {
            for (var node : typeNode.getChildren()) {
                var nodeName = node.getNodeName();
                if (limitsDefinitions.containsKey(nodeName)) {
                    if (node.hasAttribute(TypesModel.NAME_ATTRIBUTE)) {
                        var name = node.getAttributeNode(TypesModel.NAME_ATTRIBUTE);
                        var validValues = limitsDefinitions.get(nodeName);
                        if (!validValues.contains(name.getValue())) {
                            var attrValue = name.getNodeAttrValue();
                            var range = XMLPositionUtility.createRange(attrValue);
                            String message = String.format(INVALID_LIMIT_DEFINITION_MESSAGE, nodeName, name.getValue());
                            diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_LIMIT_DEFINITION_CODE));
                        }
                    }

                    if (userLimitsDefinitions.containsKey(nodeName) && node.hasAttribute(TypesModel.USER_ATTRIBUTE)) {
                        var user = node.getAttributeNode(TypesModel.USER_ATTRIBUTE);
                        var validValues = userLimitsDefinitions.get(nodeName);
                        if (!validValues.contains(user.getValue())) {
                            var attrValue = user.getNodeAttrValue();
                            var range = XMLPositionUtility.createRange(attrValue);
                            String message = String.format(INVALID_USER_LIMIT_DEFINITION_MESSAGE, nodeName, user.getValue());
                            diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_USER_LIMIT_DEFINITION_CODE));
                        }
                    }
                }
            }
        }
    }
}

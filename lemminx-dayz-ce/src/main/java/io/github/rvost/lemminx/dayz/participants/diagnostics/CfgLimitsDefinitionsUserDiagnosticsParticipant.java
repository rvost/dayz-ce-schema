package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionsModel;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Set;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class CfgLimitsDefinitionsUserDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String INVALID_LIMIT_DEFINITION_CODE = "invalid_limit_definition";
    private static final String INVALID_LIMIT_DEFINITION_MESSAGE = "%s \"%s\" does not exist.";
    private final DayzMissionService missionService;

    public CfgLimitsDefinitionsUserDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (LimitsDefinitionsModel.isUserLimitsDefinitions(domDocument)) {
            validateUserLimitsDefinitions(domDocument, list);
        }
    }

    private void validateUserLimitsDefinitions(DOMDocument document, List<Diagnostic> diagnostics) {
        var limitsDefinitions = missionService.getLimitsDefinitions();
        for (var node : document.getDocumentElement().getChildren()) {
            var nodeName = node.getNodeName();
            if (node.hasChildNodes()) {
                switch (nodeName) {
                    case LimitsDefinitionsModel.USAGEFLAGS_TAG -> {
                        validateUserNodes(node.getChildren(), limitsDefinitions.get(LimitsDefinitionsModel.USAGE_TAG), diagnostics);
                    }
                    case LimitsDefinitionsModel.VALUEFLAGS_TAG -> {
                        validateUserNodes(node.getChildren(), limitsDefinitions.get(LimitsDefinitionsModel.VALUE_TAG), diagnostics);
                    }
                    default -> {
                    }
                }
            }
        }
    }

    private static void validateUserNodes(List<DOMNode> nodes, Set<String> availableValues, List<Diagnostic> diagnostics) {
        for (var node : nodes) {
            for (var limitNode : node.getChildren()) {
                if (limitNode.hasAttribute(LimitsDefinitionsModel.NAME_ATTRIBUTE)) {
                    var kind = limitNode.getNodeName();
                    var attr = limitNode.getAttributeNode(LimitsDefinitionsModel.NAME_ATTRIBUTE);
                    if (!availableValues.contains(attr.getValue())) {
                        var attrValue = attr.getNodeAttrValue();
                        var range = XMLPositionUtility.createRange(attrValue);
                        String message = String.format(INVALID_LIMIT_DEFINITION_MESSAGE, kind, attr.getValue());
                        diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, INVALID_LIMIT_DEFINITION_CODE));
                    }
                }
            }
        }
    }
}

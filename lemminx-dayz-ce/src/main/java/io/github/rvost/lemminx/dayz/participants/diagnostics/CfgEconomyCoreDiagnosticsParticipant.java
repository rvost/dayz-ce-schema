package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
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

public class CfgEconomyCoreDiagnosticsParticipant implements IDiagnosticsParticipant {
    private static final String MISSING_FOLDER_CODE = "missing_folder";
    private static final String MISSING_FOLDER_MESSAGE = "The folder \"%s\" does not exist.";
    private static final String MISSING_FILE_CODE = "missing_file";
    private static final String MISSING_FILE_MESSAGE = "The file \"%s\" does not exist in folder \"%s\".";
    private final DayzMissionService missionService;

    public CfgEconomyCoreDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (CfgEconomyCoreModel.isCfgEconomyCore(domDocument) && missionService.isInMissionFolder(domDocument)) {
            validateCEFolders(domDocument, list);
        }
    }

    private void validateCEFolders(DOMDocument document, List<Diagnostic> diagnostics) {
        var nodes = document.getDocumentElement().getChildren();
        var missionFolders = missionService.getMissionFolders();

        for (var node : nodes) {
            if (CfgEconomyCoreModel.CE_TAG.equals(node.getNodeName())) {
                var folder = node.getAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE);
                // Case of missing attribute is handled by schema validation
                if (folder != null && !missionFolders.containsKey(folder)) {
                    var attrValue = node.getAttributeNode(CfgEconomyCoreModel.FOLDER_ATTRIBUTE).getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValue);
                    String message = String.format(MISSING_FOLDER_MESSAGE, folder);
                    diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, MISSING_FOLDER_CODE));
                } else {
                    var children = node.getChildren();
                    validateCEFiles(children, diagnostics, folder, missionFolders.get(folder));
                }
            }
        }
    }

    private void validateCEFiles(List<DOMNode> nodes, List<Diagnostic> diagnostics, String folder, Set<String> files) {
        for (var node : nodes) {
            if (CfgEconomyCoreModel.FILE_TAG.equals(node.getNodeName())) {
                var name = node.getAttribute(CfgEconomyCoreModel.NAME_ATTRIBUTE);
                // Case of missing attribute is handled by schema validation
                if (name != null && !files.contains(name)) {
                    var attrValue = node.getAttributeNode(CfgEconomyCoreModel.NAME_ATTRIBUTE).getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValue);
                    String message = String.format(MISSING_FILE_MESSAGE, name, folder);
                    diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, MISSING_FILE_CODE));
                }
            }
        }
    }
}

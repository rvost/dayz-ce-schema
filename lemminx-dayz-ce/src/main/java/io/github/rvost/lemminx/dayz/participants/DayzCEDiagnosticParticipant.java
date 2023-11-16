package io.github.rvost.lemminx.dayz.participants;

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

public class DayzCEDiagnosticParticipant implements IDiagnosticsParticipant {
    private  static final String ERROR_SOURCE = "dayz-ce-schema";
    private final DayzMissionService missionService;

    public DayzCEDiagnosticParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (!CfgEconomyCoreModel.isCfgEconomyCore(domDocument)) {
            return;
        }
        validateCEFolders(domDocument, list);
    }

    private void validateCEFolders(DOMDocument document, List<Diagnostic> diagnostics) {
        var nodes = document.getDocumentElement().getChildren();
        for (var node : nodes) {
            if (CfgEconomyCoreModel.CE_TAG.equals(node.getNodeName())) {
                var folder = node.getAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE);
                // Case of missing attribute is handled by schema validation
                if (folder !=null && !missionService.hasFolder(folder)) {
                    var attrValue = node.getAttributeNode(CfgEconomyCoreModel.FOLDER_ATTRIBUTE).getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValue);
                    String message = "The folder \"" + folder + "\" does not exist.";
                    diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, "missing_folder"));
                } else {
                    var children = node.getChildren();
                    validateCEFiles(children, diagnostics, folder);
                }
            }
        }
    }

    private void validateCEFiles(List<DOMNode> nodes, List<Diagnostic> diagnostics, String folder) {
        for (var node : nodes) {
            if (CfgEconomyCoreModel.FILE_TAG.equals(node.getNodeName())) {
                var name = node.getAttribute(CfgEconomyCoreModel.NAME_ATTRIBUTE);
                // Case of missing attribute is handled by schema validation
                if (name !=null && !missionService.hasFile(folder, name)) {
                    var attrValue = node.getAttributeNode(CfgEconomyCoreModel.NAME_ATTRIBUTE).getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValue);
                    String message = "The file \"" + name + "\" does not exist in folder \"" + folder + "\".";
                    diagnostics.add(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, "missing_file"));
                }
            }
        }
    }

}

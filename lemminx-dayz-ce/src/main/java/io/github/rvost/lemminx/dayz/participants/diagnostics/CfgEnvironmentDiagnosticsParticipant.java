package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEnvironmentModel;
import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class CfgEnvironmentDiagnosticsParticipant implements IDiagnosticsParticipant {
    private final String INVALID_TERRITORIES_FILE_CODE = "invalid_territories_file";
    private final String INVALID_TERRITORIES_FILE_MESSAGE = "File \"%s\" not found";
    private final String INVALID_TERRITORIES_FILE_REFERENCE_CODE = "invalid_territories_file_reference";
    private final String INVALID_TERRITORIES_FILE_REFERENCE_MESSAGE = "\"%s\" is not declared in files";


    private final DayzMissionService missionService;

    public CfgEnvironmentDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument domDocument, List<Diagnostic> list, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        if (CfgEnvironmentModel.match(domDocument)) {
            validateCfgEnvironment(domDocument, list);
        }
    }

    private void validateCfgEnvironment(DOMDocument document, List<Diagnostic> diagnostics) {
        var territoriesNode = document.getDocumentElement().getChildren().get(0);
        var fileNodes = territoriesNode.getChildren().stream()
                .filter(n -> CfgEnvironmentModel.FILE_TAG.equals(n.getNodeName()))
                .toList();

        var keys = getFileKeys(fileNodes);

        diagnostics.addAll(validateFileReferences(territoriesNode, keys));

        if(missionService.isInMissionFolder(document)){
            diagnostics.addAll(valifateFiles(fileNodes));
        }
    }

    private List<Diagnostic> valifateFiles(List<DOMNode> fileNodes) {
        var availableEnvFiles = missionService.getEnvFiles();

        return fileNodes.stream()
                .map(n -> n.getAttributeNode(CfgEnvironmentModel.PATH_ATTRIBUTE))
                .filter(Objects::nonNull)
                .filter(attr -> !availableEnvFiles.contains(attr.getValue()))
                .map(attr -> {
                    var attrValueRange = attr.getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValueRange);
                    var message = String.format(INVALID_TERRITORIES_FILE_MESSAGE, attr.getValue());
                    return new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, INVALID_TERRITORIES_FILE_CODE);
                })
                .toList();
    }

    private List<Diagnostic> validateFileReferences(DOMNode territoriesNode, Set<String> files) {
        return territoriesNode.getChildren().stream()
                .filter(n -> CfgEnvironmentModel.TERRITORY_TAG.equals(n.getNodeName()))
                .flatMap(n -> n.getChildren().stream())
                .map(n -> n.getAttributeNode(CfgEnvironmentModel.USABLE_ATTRIBUTE))
                .filter(Objects::nonNull)
                .filter(attr -> !files.contains(attr.getValue()))
                .map(attr -> {
                    var attrValueRange = attr.getNodeAttrValue();
                    var range = XMLPositionUtility.createRange(attrValueRange);
                    var message = String.format(INVALID_TERRITORIES_FILE_REFERENCE_MESSAGE, attr.getValue());
                    return new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, INVALID_TERRITORIES_FILE_REFERENCE_CODE);
                })
                .toList();
    }

    private Set<String> getFileKeys(List<DOMNode> fileNodes) {
        return fileNodes.stream()
                .map(n -> n.getAttributeNode(CfgEnvironmentModel.PATH_ATTRIBUTE))
                .filter(Objects::nonNull)
                .map(DOMAttr::getValue)
                .map(CfgEnvironmentModel::getUsableKeyFromPath)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }
}

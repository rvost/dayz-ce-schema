package io.github.rvost.lemminx.dayz.participants.codeaction;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import io.github.rvost.lemminx.dayz.model.MissionModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import io.github.rvost.lemminx.dayz.participants.diagnostics.MissionDiagnosticsParticipant;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

public class FixFileTypeCodeAction implements ICodeActionParticipant {
    private final DayzMissionService missionService;

    public FixFileTypeCodeAction(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doCodeAction(ICodeActionRequest request, List<CodeAction> codeActions, CancelChecker cancelChecker) {
        var diagnostic = request.getDiagnostic();
        var document = request.getDocument();
        var expectedType = MissionModel.TryGetFileType(document);

        if (!ParticipantsUtils.match(diagnostic, MissionDiagnosticsParticipant.FILE_TYPE_MISMATCH_CODE) || expectedType.isEmpty()) {
            return;
        }


        var cfgEconomyDocument = org.eclipse.lemminx.utils.DOMUtils.loadDocument(
                getCfgEconomyCoreURI(),
                document.getResolverExtensionManager()
        );

        try {
            var docPath = Path.of(new URI(document.getDocumentURI()));
            var folder = docPath.getParent().getFileName().toString();
            var file = docPath.getFileName().toString();
            var fileNode = cfgEconomyDocument.getDocumentElement().getChildren().stream()
                    .filter(n -> CfgEconomyCoreModel.CE_TAG.equals(n.getNodeName()))
                    .filter(n -> folder.equals(n.getAttribute(CfgEconomyCoreModel.FOLDER_ATTRIBUTE)))
                    .flatMap(n -> n.getChildren().stream())
                    .filter(n -> file.equals(n.getAttribute(CfgEconomyCoreModel.NAME_ATTRIBUTE)))
                    .findFirst();

            if (fileNode.isPresent()) {
                var replaceText = expectedType.get().toString().toLowerCase();
                var attr = fileNode.get().getAttributeNode(CfgEconomyCoreModel.TYPE_ATTRIBUTE);
                var range = XMLPositionUtility.selectAttributeValue(attr, true);

                var title = String.format("Change file type to '%s'", replaceText);
                var ca = CodeActionFactory.replace(title,
                        range,
                        replaceText,
                        cfgEconomyDocument.getTextDocument(),
                        diagnostic
                );

                codeActions.add(ca);
            }

        } catch (URISyntaxException ignored) {
        }
    }

    private String getCfgEconomyCoreURI() {
        return missionService.missionRoot.resolve(CfgEconomyCoreModel.CFGECONOMYCORE_XML).toUri().toString();
    }

}

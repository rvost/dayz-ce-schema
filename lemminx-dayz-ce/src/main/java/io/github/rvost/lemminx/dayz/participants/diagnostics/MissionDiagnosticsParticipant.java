package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.DayzFileType;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class MissionDiagnosticsParticipant implements IDiagnosticsParticipant {

    public static final String FILE_NOT_REGISTERED_CODE = "file_not_registered";
    private static final String FILE_NOT_REGISTERED_MESSAGE = "File \"%s\" is not registered in cfgeconomycore.xml.";

    private static final String FILE_OUT_OF_FOLDER_CODE = "file_out_of_folder";
    private static final String FILE_OUT_OF_FOLDER_MESSAGE = "File \"%s\" is not a part of the mission folder.";

    private final DayzMissionService missionService;

    public MissionDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument document, List<Diagnostic> diagnostics, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        var rootTag = document.getDocumentElement();
        var docMatch = Arrays.stream(DayzFileType.values()).anyMatch(v -> v.RootTag.equals(rootTag.getNodeName()));

        if (docMatch) {
            validateFileInMissionFolder(document).ifPresentOrElse(diagnostics::add,
                    () -> validateCustomFileRegistration(document).ifPresent(diagnostics::add));
        }
    }

    private Optional<Diagnostic> validateFileInMissionFolder(DOMDocument document) {
        try {
            var docPath = Path.of(new URI(document.getDocumentURI())).toAbsolutePath();
            var missionPath = missionService.missionRoot.toAbsolutePath();

            if (!docPath.startsWith(missionPath)) {
                var rootTag = document.getDocumentElement();
                var range = XMLPositionUtility.createRange(rootTag.getStartTagOpenOffset(), rootTag.getStartTagCloseOffset(), document);
                var message = String.format(FILE_OUT_OF_FOLDER_MESSAGE, docPath.getFileName());
                return Optional.of(new Diagnostic(range, message, DiagnosticSeverity.Information, ERROR_SOURCE, FILE_OUT_OF_FOLDER_CODE));
            }
        } catch (URISyntaxException ignored) {
        }
        return Optional.empty();
    }

    private Optional<Diagnostic> validateCustomFileRegistration(DOMDocument document) {
        try {
            var docPath = Path.of(new URI(document.getDocumentURI()));
            var isRegistered = missionService.isRegistered(docPath);
            if (!isRegistered) {
                var rootTag = document.getDocumentElement();
                var range = XMLPositionUtility.createRange(rootTag.getStartTagOpenOffset(), rootTag.getStartTagCloseOffset(), document);
                var message = String.format(FILE_NOT_REGISTERED_MESSAGE, docPath.getFileName());
                return Optional.of(new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, FILE_NOT_REGISTERED_CODE));
            }
        } catch (URISyntaxException ignored) {
        }
        return Optional.empty();
    }

}

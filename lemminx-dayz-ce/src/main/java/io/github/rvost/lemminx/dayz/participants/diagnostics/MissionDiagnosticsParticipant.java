package io.github.rvost.lemminx.dayz.participants.diagnostics;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.MissionModel;
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
import java.util.List;
import java.util.Optional;

import static io.github.rvost.lemminx.dayz.participants.diagnostics.DiagnosticsUtils.ERROR_SOURCE;

public class MissionDiagnosticsParticipant implements IDiagnosticsParticipant {

    public static final String FILE_NOT_REGISTERED_CODE = "custom_file_not_registered";
    private static final String FILE_NOT_REGISTERED_MESSAGE = "File \"%1$s\" is not registered in cfgeconomycore.xml.\n"
            + "\"%1$s\" can be registered as custom \"%2$s\" file.";

    public static final String FILE_OUT_OF_FOLDER_CODE = "custom_file_out_of_folder";
    private static final String FILE_OUT_OF_FOLDER_MESSAGE = "File \"%1$s\" is not a part of the mission folder.\n"
            + "\"%1$s\" can be registered as a custom \"%2$s\" file, but located outside the currently open mission folder.";

    public static final String FILE_TYPE_MISMATCH_CODE = "custom_file_type_mismatch";
    private static final String FILE_TYPE_MISMATCH_MESSAGE = "Incorrect file type registered in cfgeconomycore.xml.\n"
            + "File is registered as \"%s\", but actual type appears to be \"%s\".";

    private final DayzMissionService missionService;

    public MissionDiagnosticsParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public void doDiagnostics(DOMDocument document, List<Diagnostic> diagnostics, XMLValidationSettings xmlValidationSettings, CancelChecker cancelChecker) {
        var docMatch = MissionModel.IsCustomFile(document);

        if (docMatch) {
            var inFolderDiagnostics = validateFileInMissionFolder(document);

            if (inFolderDiagnostics.isPresent()) {
                diagnostics.add(inFolderDiagnostics.get());
            } else {
                validateCustomFileRegistration(document).ifPresent(diagnostics::add);
                validateCustomFileType(document).ifPresent(diagnostics::add);
            }
        }
    }

    private Optional<Diagnostic> validateFileInMissionFolder(DOMDocument document) {
        try {
            var docPath = Path.of(new URI(document.getDocumentURI())).toAbsolutePath();
            var missionPath = missionService.missionRoot.toAbsolutePath();

            if (!docPath.startsWith(missionPath)) {
                var rootTag = document.getDocumentElement();
                var range = XMLPositionUtility.createRange(rootTag.getStartTagOpenOffset(), rootTag.getStartTagCloseOffset(), document);
                var fileType = MissionModel.TryGetFileType(document).get();
                var message = String.format(FILE_OUT_OF_FOLDER_MESSAGE, docPath.getFileName(), fileType.toString().toLowerCase());
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
                var fileType = MissionModel.TryGetFileType(document).get();
                var message = String.format(FILE_NOT_REGISTERED_MESSAGE, docPath.getFileName(), fileType.toString().toLowerCase());
                return Optional.of(new Diagnostic(range, message, DiagnosticSeverity.Warning, ERROR_SOURCE, FILE_NOT_REGISTERED_CODE));
            }
        } catch (URISyntaxException ignored) {
        }
        return Optional.empty();
    }

    private Optional<Diagnostic> validateCustomFileType(DOMDocument document) {
        try {
            var docPath = Path.of(new URI(document.getDocumentURI()));
            var registeredType = missionService.getRegisteredType(docPath);
            var actualType = MissionModel.TryGetFileType(document);

            if (registeredType.isPresent() && !registeredType.equals(actualType)) {
                var rootTag = document.getDocumentElement();
                var range = XMLPositionUtility.createRange(rootTag.getStartTagOpenOffset(), rootTag.getStartTagCloseOffset(), document);
                var message = String.format(FILE_TYPE_MISMATCH_MESSAGE,
                        registeredType.get().toString().toLowerCase(),
                        actualType.get().toString().toLowerCase());
                return Optional.of(new Diagnostic(range, message, DiagnosticSeverity.Error, ERROR_SOURCE, FILE_TYPE_MISMATCH_CODE));
            }
        } catch (URISyntaxException ignored) {
        }
        return Optional.empty();
    }

}

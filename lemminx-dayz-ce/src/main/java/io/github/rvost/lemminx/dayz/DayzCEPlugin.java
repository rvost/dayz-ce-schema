package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.participants.DayzCECompletionParticipant;
import io.github.rvost.lemminx.dayz.participants.DayzCEDiagnosticParticipant;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.completion.ICompletionParticipant;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lsp4j.InitializeParams;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DayzCEPlugin implements IXMLExtension  {
    private static final Logger LOGGER = Logger.getLogger(DayzCEPlugin.class.getName());

    private DayzMissionService missionService;
    private ICompletionParticipant completionParticipant;
    private IDiagnosticsParticipant diagnosticsParticipant;

    @Override
    public void doSave(ISaveContext context) {
        // Intentionally left blank.
    }

    @Override
    public void start(InitializeParams params, XMLExtensionsRegistry registry) {
        // Register here completion, hover, etc participants
        LOGGER.log(Level.INFO, "DayZ CE Server started");

         var folders = params.getWorkspaceFolders();
         missionService = DayzMissionService.create(folders);

        completionParticipant = new DayzCECompletionParticipant(missionService);
        registry.registerCompletionParticipant(completionParticipant);

        diagnosticsParticipant = new DayzCEDiagnosticParticipant(missionService);
        registry.registerDiagnosticsParticipant(diagnosticsParticipant);
    }

    @Override
    public void stop(XMLExtensionsRegistry registry) {
        // Unregister here completion, hover, etc participants
        LOGGER.log(Level.INFO, "DayZ CE Server stopped");

        registry.unregisterCompletionParticipant(completionParticipant);
        registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
    }
}

package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.participants.completion.*;
import io.github.rvost.lemminx.dayz.participants.diagnostics.*;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.completion.ICompletionParticipant;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lsp4j.InitializeParams;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DayzCEPlugin implements IXMLExtension {
    private static final Logger LOGGER = Logger.getLogger(DayzCEPlugin.class.getName());

    private DayzMissionService missionService;
    private final List<ICompletionParticipant> completionParticipants = new ArrayList<>();
    private final List<IDiagnosticsParticipant> diagnosticsParticipants = new ArrayList<>();

    @Override
    public void doSave(ISaveContext context) {
        // Intentionally left blank.
    }

    @Override
    public void start(InitializeParams params, XMLExtensionsRegistry registry) {
        // Register here completion, hover, etc participants
        LOGGER.log(Level.INFO, "DayZ CE Server started");

        var folders = params.getWorkspaceFolders();
        try {
            missionService = DayzMissionService.create(folders);
            registerCompletionParticipants(registry, missionService);
            registerDiagnosticsParticipants(registry, missionService);
            missionService.start();
        } catch (Exception ignored) {

        }
    }

    @Override
    public void stop(XMLExtensionsRegistry registry) {
        // Unregister here completion, hover, etc participants
        LOGGER.log(Level.INFO, "DayZ CE Server stopped");

        unregisterCompletionParticipants(registry);
        unregisterDiagnosticsParticipants(registry);
        missionService.close();
    }

    private void registerCompletionParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (completionParticipants.isEmpty()) {
            completionParticipants.add(new CfgEconomyCoreCompletionParticipant(missionService));
            completionParticipants.add(new CfgLimitsDefinitionsUserCompletionParticipant(missionService));
            completionParticipants.add(new TypesCompletionParticipant(missionService));
            completionParticipants.add(new SpawnableTypesCompletionParticipant(missionService));
            completionParticipants.add(new CfgRandomPresetsCompletionParticipant(missionService));

            completionParticipants.forEach(registry::registerCompletionParticipant);
        }
    }

    private void unregisterCompletionParticipants(XMLExtensionsRegistry registry) {
        completionParticipants.forEach(registry::unregisterCompletionParticipant);
        completionParticipants.clear();
    }

    private void registerDiagnosticsParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (diagnosticsParticipants.isEmpty()) {
            diagnosticsParticipants.add(new CfgEconomyCoreDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new CfgLimitsDefinitionsUserDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new TypesDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new SpawnableTypesDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new CfgRandomPresetsDiagnosticsParticipant(missionService));

            diagnosticsParticipants.forEach(registry::registerDiagnosticsParticipant);
        }
    }

    private void unregisterDiagnosticsParticipants(XMLExtensionsRegistry registry) {
        diagnosticsParticipants.forEach(registry::unregisterDiagnosticsParticipant);
        diagnosticsParticipants.clear();
    }
}

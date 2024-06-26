package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.commands.ComputeRefactorEditHandler;
import io.github.rvost.lemminx.dayz.commands.CreateNewFileHandler;
import io.github.rvost.lemminx.dayz.participants.codeaction.AddCustomFileCodeAction;
import io.github.rvost.lemminx.dayz.participants.codeaction.FixFileTypeCodeAction;
import io.github.rvost.lemminx.dayz.participants.codeaction.RefactorCustomFilesCodeAction;
import io.github.rvost.lemminx.dayz.participants.completion.*;
import io.github.rvost.lemminx.dayz.participants.diagnostics.*;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
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
    private final List<ICodeActionParticipant> codeActionParticipants = new ArrayList<>();
    private DayzSchemaURIResolver uriResolver;

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
            registerCodeActionParticipants(registry, missionService);

            var commandService = registry.getCommandService();
            commandService.registerCommand(ComputeRefactorEditHandler.COMMAND,
                    new ComputeRefactorEditHandler(registry.getDocumentProvider(), missionService));
            commandService.registerCommand(CreateNewFileHandler.COMMAND,
                    new CreateNewFileHandler(missionService, registry.getResolverExtensionManager()));

            uriResolver = new DayzSchemaURIResolver(registry.getDocumentProvider());
            registry.getResolverExtensionManager().registerResolver(uriResolver);

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
        unregisterCodeActionParticipants(registry);

        registry.getCommandService().unregisterCommand(ComputeRefactorEditHandler.COMMAND);
        registry.getCommandService().unregisterCommand(CreateNewFileHandler.COMMAND);

        registry.getResolverExtensionManager().unregisterResolver(uriResolver);

        missionService.close();
    }

    private void registerCompletionParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (completionParticipants.isEmpty()) {
            completionParticipants.add(new CfgEconomyCoreCompletionParticipant(missionService));
            completionParticipants.add(new CfgLimitsDefinitionsUserCompletionParticipant(missionService));
            completionParticipants.add(new TypesCompletionParticipant(missionService));
            completionParticipants.add(new SpawnableTypesCompletionParticipant(missionService));
            completionParticipants.add(new CfgRandomPresetsCompletionParticipant(missionService));
            completionParticipants.add(new EventsCompletionParticipant(missionService));
            completionParticipants.add(new CfgEventSpawnsCompletionParticipant(missionService));
            completionParticipants.add(new CfgEnvironmentCompletionParticipant(missionService));
            completionParticipants.add(new MapGroupProtoCompletionParticipant(missionService));
            completionParticipants.add(new MapGroupPosCompletionParticipant(missionService));

            completionParticipants.forEach(registry::registerCompletionParticipant);
        }
    }

    private void unregisterCompletionParticipants(XMLExtensionsRegistry registry) {
        completionParticipants.forEach(registry::unregisterCompletionParticipant);
        completionParticipants.clear();
    }

    private void registerDiagnosticsParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (diagnosticsParticipants.isEmpty()) {
            diagnosticsParticipants.add(new MissionDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new CfgEconomyCoreDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new CfgLimitsDefinitionsUserDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new TypesDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new SpawnableTypesDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new CfgRandomPresetsDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new EventsDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new CfgEventSpawnsDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new GlobalsDiagnosticsParticipant());
            diagnosticsParticipants.add(new CfgEnvironmentDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new MapGroupProtoDiagnosticsParticipant(missionService));
            diagnosticsParticipants.add(new MapGroupPosDiagnosticsParticipant(missionService));

            diagnosticsParticipants.forEach(registry::registerDiagnosticsParticipant);
        }
    }

    private void unregisterDiagnosticsParticipants(XMLExtensionsRegistry registry) {
        diagnosticsParticipants.forEach(registry::unregisterDiagnosticsParticipant);
        diagnosticsParticipants.clear();
    }

    private void registerCodeActionParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (codeActionParticipants.isEmpty()) {
            codeActionParticipants.add(new AddCustomFileCodeAction(missionService));
            codeActionParticipants.add(new FixFileTypeCodeAction(missionService));
            codeActionParticipants.add(new RefactorCustomFilesCodeAction(missionService));

            codeActionParticipants.forEach(registry::registerCodeActionParticipant);
        }
    }

    private void unregisterCodeActionParticipants(XMLExtensionsRegistry registry) {
        codeActionParticipants.forEach(registry::unregisterCodeActionParticipant);
        codeActionParticipants.clear();
    }
}

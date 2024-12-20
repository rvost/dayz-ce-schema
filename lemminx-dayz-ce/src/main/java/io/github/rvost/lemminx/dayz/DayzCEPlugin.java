package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.commands.*;
import io.github.rvost.lemminx.dayz.participants.codeaction.*;
import io.github.rvost.lemminx.dayz.participants.codelens.FlagsCodeLensParticipant;
import io.github.rvost.lemminx.dayz.participants.codelens.RandomPresetsCodeLensParticipant;
import io.github.rvost.lemminx.dayz.participants.codelens.UserFlagsCodeLensParticipant;
import io.github.rvost.lemminx.dayz.participants.completion.*;
import io.github.rvost.lemminx.dayz.participants.definition.CfgEventSpawnsDefinitionParticipant;
import io.github.rvost.lemminx.dayz.participants.definition.FlagsDefinitionParticipant;
import io.github.rvost.lemminx.dayz.participants.definition.MapGroupDefinitionParticipant;
import io.github.rvost.lemminx.dayz.participants.definition.RandomPresetDefinitionParticipant;
import io.github.rvost.lemminx.dayz.participants.diagnostics.*;
import io.github.rvost.lemminx.dayz.participants.hover.CfgWeatherHoverParticipant;
import io.github.rvost.lemminx.dayz.participants.hover.EventsHoverParticipant;
import io.github.rvost.lemminx.dayz.participants.hover.GlobalsHoverParticipant;
import io.github.rvost.lemminx.dayz.participants.hover.TypesHoverParticipant;
import io.github.rvost.lemminx.dayz.participants.link.CfgEconomyCoreDocumentLinkParticipant;
import io.github.rvost.lemminx.dayz.participants.link.CfgEnvironmentDocumentLinkParticipant;
import io.github.rvost.lemminx.dayz.participants.reference.*;
import io.github.rvost.lemminx.dayz.participants.rename.EventGroupRenameParticipant;
import io.github.rvost.lemminx.dayz.participants.rename.EventsRenameParticipant;
import io.github.rvost.lemminx.dayz.participants.rename.RandomPresetRenameParticipant;
import io.github.rvost.lemminx.dayz.participants.rename.UserFlagRenameParticipant;
import org.eclipse.lemminx.services.extensions.*;
import org.eclipse.lemminx.services.extensions.codeaction.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.codelens.ICodeLensParticipant;
import org.eclipse.lemminx.services.extensions.commands.IXMLCommandService;
import org.eclipse.lemminx.services.extensions.completion.ICompletionParticipant;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.services.extensions.hover.IHoverParticipant;
import org.eclipse.lemminx.services.extensions.rename.IRenameParticipant;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.eclipse.lsp4j.InitializeParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DayzCEPlugin implements IXMLExtension {
    private static final Logger LOGGER = Logger.getLogger(DayzCEPlugin.class.getName());

    private DayzMissionService missionService;
    private final List<ICompletionParticipant> completionParticipants = new ArrayList<>();
    private final List<IDiagnosticsParticipant> diagnosticsParticipants = new ArrayList<>();
    private final List<ICodeActionParticipant> codeActionParticipants = new ArrayList<>();
    private final List<IHoverParticipant> hoverParticipants = new ArrayList<>();
    private final List<IDefinitionParticipant> definitionParticipants = new ArrayList<>();
    private final List<IDocumentLinkParticipant> linkParticipants = new ArrayList<>();
    private final List<IReferenceParticipant> referenceParticipants = new ArrayList<>();
    private final List<IRenameParticipant> renameParticipants = new ArrayList<>();
    private final List<ICodeLensParticipant> codeLensParticipants = new ArrayList<>();
    private final Map<String, IXMLCommandService.IDelegateCommandHandler> serverCommands = new HashMap<>();
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
            registerHoverParticipants(registry, missionService);
            registerDefinitionParticipants(registry, missionService);
            registerLinkParticipants(registry, missionService);
            registerReferenceParticipants(registry, missionService);
            registerRenameParticipants(registry, missionService);
            registerCodeLensParticipant(registry, missionService);

            registerServerCommands(registry, missionService);

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
        unregisterHoverParticipants(registry);
        unregisterDefinitionParticipants(registry);
        unregisterLinkParticipants(registry);
        unregisterReferenceParticipants(registry);
        unregisterRenameParticipants(registry);
        unregisterCodeLensParticipant(registry);

        unregisterServerCommands(registry);

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
            completionParticipants.add(new CfgEventGroupsCompletionParticipant(missionService));
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
            diagnosticsParticipants.add(new CfgEventGroupsDiagnosticsParticipant(missionService));
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
            codeActionParticipants.add(new CopyCfgEventSpawnsCodeAction(missionService));
            codeActionParticipants.add(new RefactorPresetCodeAction(missionService));
            codeActionParticipants.add(new RefactorLimitFlagsCodeAction(missionService));

            codeActionParticipants.forEach(registry::registerCodeActionParticipant);
        }
    }

    private void unregisterCodeActionParticipants(XMLExtensionsRegistry registry) {
        codeActionParticipants.forEach(registry::unregisterCodeActionParticipant);
        codeActionParticipants.clear();
    }

    private void registerHoverParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (hoverParticipants.isEmpty()) {
            hoverParticipants.add(new TypesHoverParticipant(missionService));
            hoverParticipants.add(new EventsHoverParticipant());
            hoverParticipants.add(new CfgWeatherHoverParticipant());
            hoverParticipants.add(new GlobalsHoverParticipant());

            hoverParticipants.forEach(registry::registerHoverParticipant);
        }
    }

    private void unregisterHoverParticipants(XMLExtensionsRegistry registry) {
        hoverParticipants.forEach(registry::unregisterHoverParticipant);
        hoverParticipants.clear();
    }

    private void registerDefinitionParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (definitionParticipants.isEmpty()) {
            definitionParticipants.add(new RandomPresetDefinitionParticipant(missionService));
            definitionParticipants.add(new FlagsDefinitionParticipant(missionService));
            definitionParticipants.add(new CfgEventSpawnsDefinitionParticipant(missionService));
            definitionParticipants.add(new MapGroupDefinitionParticipant(missionService));

            definitionParticipants.forEach(registry::registerDefinitionParticipant);
        }
    }

    private void unregisterDefinitionParticipants(XMLExtensionsRegistry registry) {
        definitionParticipants.forEach(registry::unregisterDefinitionParticipant);
        definitionParticipants.clear();
    }

    private void registerLinkParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (linkParticipants.isEmpty()) {
            linkParticipants.add(new CfgEconomyCoreDocumentLinkParticipant(missionService));
            linkParticipants.add(new CfgEnvironmentDocumentLinkParticipant(missionService));

            linkParticipants.forEach(registry::registerDocumentLinkParticipant);
        }
    }

    private void unregisterLinkParticipants(XMLExtensionsRegistry registry) {
        linkParticipants.forEach(registry::unregisterDocumentLinkParticipant);
        linkParticipants.clear();
    }

    private void registerReferenceParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (referenceParticipants.isEmpty()) {
            referenceParticipants.add(new EventsReferenceParticipant(missionService));
            referenceParticipants.add(new TypesReferenceParticipant(missionService));
            referenceParticipants.add(new RandomPresetReferenceParticipant(missionService));
            referenceParticipants.add(new CfgEventGroupsReferenceParticipant(missionService));
            referenceParticipants.add(new MapGroupProtoReferenceParticipant(missionService));
            referenceParticipants.add(new FlagReferenceParticipant(missionService));
            referenceParticipants.add(new UserFlagReferenceParticipant(missionService));

            referenceParticipants.forEach(registry::registerReferenceParticipant);
        }
    }

    private void unregisterReferenceParticipants(XMLExtensionsRegistry registry) {
        referenceParticipants.forEach(registry::unregisterReferenceParticipant);
        referenceParticipants.clear();
    }

    private void registerRenameParticipants(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (renameParticipants.isEmpty()) {
            renameParticipants.add(new UserFlagRenameParticipant(missionService));
            renameParticipants.add(new RandomPresetRenameParticipant(missionService));
            renameParticipants.add(new EventGroupRenameParticipant(missionService));
            renameParticipants.add(new EventsRenameParticipant(missionService));

            renameParticipants.forEach(registry::registerRenameParticipant);
        }
    }

    private void unregisterRenameParticipants(XMLExtensionsRegistry registry) {
        renameParticipants.forEach(registry::unregisterRenameParticipant);
        renameParticipants.clear();
    }

    private void registerCodeLensParticipant(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (codeLensParticipants.isEmpty()) {
            codeLensParticipants.add(new RandomPresetsCodeLensParticipant(missionService));
            codeLensParticipants.add(new FlagsCodeLensParticipant(missionService));
            codeLensParticipants.add(new UserFlagsCodeLensParticipant(missionService));

            codeLensParticipants.forEach(registry::registerCodeLensParticipant);
        }
    }

    private void unregisterCodeLensParticipant(XMLExtensionsRegistry registry) {
        codeLensParticipants.forEach(registry::unregisterCodeLensParticipant);
        codeLensParticipants.clear();
    }

    private void registerServerCommands(XMLExtensionsRegistry registry, DayzMissionService missionService) {
        if (serverCommands.isEmpty()) {
            serverCommands.put(ComputeCustomFileRefactorHandler.COMMAND,
                    new ComputeCustomFileRefactorHandler(registry.getDocumentProvider()));
            serverCommands.put(CreateNewFileHandler.COMMAND,
                    new CreateNewFileHandler(missionService, registry.getResolverExtensionManager()));
            serverCommands.put(ComputeEventsSpawnsCopyHandler.COMMAND,
                    new ComputeEventsSpawnsCopyHandler(registry.getDocumentProvider(), missionService));
            serverCommands.put(ComputeExtractRandomPreset.COMMAND,
                    new ComputeExtractRandomPreset(registry.getDocumentProvider(), missionService));
            serverCommands.put(ComputeExtractUserFlag.COMMAND,
                    new ComputeExtractUserFlag(registry.getDocumentProvider(), missionService));

            serverCommands.forEach(registry.getCommandService()::registerCommand);
        }
    }

    private void unregisterServerCommands(XMLExtensionsRegistry registry) {
        var commandService = registry.getCommandService();
        serverCommands.keySet().forEach(commandService::unregisterCommand);
        serverCommands.clear();
    }
}

package io.github.rvost.lemminx.dayz.commands;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import io.github.rvost.lemminx.dayz.model.DayzFileType;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.services.extensions.commands.ArgumentsUtils;
import org.eclipse.lemminx.services.extensions.commands.IXMLCommandService;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.uriresolver.URIResolverExtensionManager;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class CreateNewFileHandler implements IXMLCommandService.IDelegateCommandHandler {
    public static final String COMMAND = "dayz-ce-schema.createNewFile";

    private final DayzMissionService missionService;
    private final URIResolverExtensionManager uriResolver;

    public CreateNewFileHandler(DayzMissionService missionService, URIResolverExtensionManager uriResolver) {
        this.missionService = missionService;
        this.uriResolver = uriResolver;
    }

    @Override
    public Object executeCommand(ExecuteCommandParams params, SharedSettings sharedSettings, CancelChecker cancelChecker) throws Exception {
        var targetUri = ArgumentsUtils.getArgAt(params, 0, String.class);
        var typeCode = ArgumentsUtils.getArgAt(params, 1, Integer.class);
        if (typeCode == null) {
            return null;
        }
        var docType = DayzFileType.values()[typeCode];

        var edit = CodeActionFactory.createFileEdit(targetUri, docType.getEmptyFileContent());
        try {
            var docPath = Path.of(new URI(targetUri));
            var cfgEconomyEdit = CfgEconomyCoreModel.getNewFileEdit(docPath, docType, missionService, uriResolver);
            edit.getDocumentChanges().add(Either.forLeft(cfgEconomyEdit));
            return edit;
        } catch (URISyntaxException ex) {
            return null;
        }
    }

}

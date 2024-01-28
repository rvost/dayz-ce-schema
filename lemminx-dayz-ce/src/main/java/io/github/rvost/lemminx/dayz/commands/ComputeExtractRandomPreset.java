package io.github.rvost.lemminx.dayz.commands;

import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import io.github.rvost.lemminx.dayz.model.SpawnableTypesModel;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.IXMLDocumentProvider;
import org.eclipse.lemminx.services.extensions.commands.AbstractDOMDocumentCommandHandler;
import org.eclipse.lemminx.services.extensions.commands.ArgumentsUtils;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.utils.DOMUtils;
import org.eclipse.lemminx.utils.TextEditUtils;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;

public class ComputeExtractRandomPreset extends AbstractDOMDocumentCommandHandler {
    public static final String COMMAND = "dayz-ce-schema.computeExtractRandomPreset";

    private final DayzMissionService missionService;

    public ComputeExtractRandomPreset(IXMLDocumentProvider documentProvider, DayzMissionService missionService) {
        super(documentProvider);
        this.missionService = missionService;
    }

    @Override
    protected Object executeCommand(DOMDocument document, ExecuteCommandParams params, SharedSettings sharedSettings, CancelChecker cancelChecker) throws Exception {
        var selectedRange = ArgumentsUtils.getArgAt(params, 1, Range.class);
        var presetName = ArgumentsUtils.getArgAt(params, 2, String.class);

        var edits = new ArrayList<Either<TextDocumentEdit, ResourceOperation>>();

        var presetEdit = computeRandomPresetsEdit(document, selectedRange, presetName);
        edits.add(Either.forLeft(presetEdit));
        var spawnableTypesEdit = computeSpawnableTypesEdit(document, selectedRange, presetName);
        edits.add(Either.forLeft(spawnableTypesEdit));

        return new WorkspaceEdit(edits);
    }

    private TextDocumentEdit computeRandomPresetsEdit(DOMDocument source, Range selectedRange, String presetName) throws BadLocationException {
        var actualStartOffset = source.offsetAt(selectedRange.getStart());
        var actualEndOffset = source.offsetAt(selectedRange.getEnd());

        var sourceText = source.getTextDocument().getText().substring(actualStartOffset, actualEndOffset);
        var nameText = String.format(" %s=\"%s\" ", RandomPresetsModel.NAME_ATTRIBUTE, presetName);
        var editText = sourceText.replaceFirst("\\s", nameText);

        var target = resolveRandomPresetsDocument(source);
        if (target == null) {
            return null;
        }
        var insertOffset = target.getDocumentElement().getLastChild().getEnd();
        var insertRange = XMLPositionUtility.createRange(insertOffset, insertOffset, target);
        var te = new TextEdit(insertRange, "\n\t" + editText);

        return TextEditUtils.creatTextDocumentEdit(target, List.of(te));
    }

    private TextDocumentEdit computeSpawnableTypesEdit(DOMDocument source, Range selectedRange, String presetName) throws BadLocationException {
        var node = source.findNodeAt(source.offsetAt(selectedRange.getStart()) + 1);

        var tagName = node.getLocalName();
        var editText = String.format("<%s %s=\"%s\" />", tagName, SpawnableTypesModel.PRESET_ATTRIBUTE, presetName);

        var te = new TextEdit(selectedRange, editText);
        return TextEditUtils.creatTextDocumentEdit(source, List.of(te));
    }

    private DOMDocument resolveRandomPresetsDocument(DOMDocument document) {
        var targetUri = missionService.missionRoot.resolve(RandomPresetsModel.CFGRANDOMPRESETS_FILE).toUri();
        return DOMUtils.loadDocument(
                targetUri.toString(),
                document.getResolverExtensionManager()
        );
    }

}

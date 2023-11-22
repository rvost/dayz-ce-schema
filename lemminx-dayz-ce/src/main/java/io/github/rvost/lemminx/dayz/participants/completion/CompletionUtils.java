package io.github.rvost.lemminx.dayz.participants.completion;

import org.eclipse.lemminx.services.extensions.completion.ICompletionRequest;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class CompletionUtils {
    private CompletionUtils() {
    }

    public static CompletionItem toCompletionItem(String option, ICompletionRequest request, Range editRange, CompletionItemKind kind) {
        var item = new CompletionItem();
        var insertText = request.getInsertAttrValue(option);
        item.setLabel(insertText);
        item.setFilterText(insertText);
        item.setKind(kind);
        item.setTextEdit(Either.forLeft(new TextEdit(editRange, insertText)));
        return item;
    }
}

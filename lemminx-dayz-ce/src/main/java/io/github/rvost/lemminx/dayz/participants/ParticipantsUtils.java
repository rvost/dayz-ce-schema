package io.github.rvost.lemminx.dayz.participants;

import com.google.common.primitives.Ints;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.services.extensions.hover.IHoverRequest;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ParticipantsUtils {
    public static boolean match(Diagnostic diagnostic, String code) {
        if (diagnostic == null || diagnostic.getCode() == null || !diagnostic.getCode().isLeft()) {
            return false;
        }

        return code == null ? diagnostic.getCode().getLeft() == null :
                code.equals(diagnostic.getCode().getLeft());
    }

    public static String toReadableTime(int seconds) {
        var remainingSeconds = seconds % 60;
        var minutes = seconds % 3600 / 60;
        var hours = seconds % 86400 / 3600;
        var days = seconds / 86400;

        var result = String.format("%02dm %02ds", minutes, remainingSeconds);
        if (days > 0) {
            result = String.format("%dd %02dh ", days, hours) + result;
        } else if (hours > 0) {
            result = String.format("%dh ", hours) + result;
        }

        return result;
    }

    public static Hover hoverForTimeInterval(IHoverRequest request, CancelChecker cancelChecker) {
        var content = request.getNode().getTextContent();

        return Optional.ofNullable(Ints.tryParse(content))
                .map(ParticipantsUtils::toReadableTime)
                .map(str -> new Hover(new MarkupContent(MarkupKind.PLAINTEXT, str)))
                .orElse(null);
    }

    public static LocationLink toDefinitionLocationLink(Path target, Range targetRange, DOMNode originNode) {
        if (target == null) {
            return null;
        }
        return new LocationLink(target.toUri().toString(), targetRange, targetRange,
                XMLPositionUtility.createRange(originNode));
    }

    public static Optional<DOMNode> tryGetStartNode(DOMDocument document, Range range) {
        try {

            var startOffset = document.offsetAt(range.getStart());
            var endOffset = document.offsetAt(range.getEnd());

            return document.getDocumentElement().getChildren().stream()
                    .filter(n -> inRange(n.getStart(), startOffset, endOffset) || inRange(n.getEnd(), startOffset, endOffset))
                    .findAny();
        } catch (BadLocationException ignored) {
            return Optional.empty();
        }
    }

    public static boolean inRange(int offset, int startOffset, int endOffset) {
        return offset >= startOffset && offset <= endOffset;
    }

    public static Optional<DOMNode> tryGetNodeAtSelection(DOMDocument document, Range range) {
        try {
            return Optional.ofNullable(document.findNodeAt(document.offsetAt(range.getStart())));
        } catch (BadLocationException e) {
            return Optional.empty();
        }
    }

    public static List<TextEdit> toReplaceEdits(String value, List<Range> ranges) {
        return ranges.stream()
                .map(r -> new TextEdit(r, value))
                .toList();
    }
}

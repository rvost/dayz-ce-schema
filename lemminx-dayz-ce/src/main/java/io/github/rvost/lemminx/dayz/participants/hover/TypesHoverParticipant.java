package io.github.rvost.lemminx.dayz.participants.hover;

import com.google.common.primitives.Ints;
import io.github.rvost.lemminx.dayz.DayzMissionService;
import io.github.rvost.lemminx.dayz.model.TypesModel;
import org.eclipse.lemminx.services.extensions.hover.HoverParticipantAdapter;
import org.eclipse.lemminx.services.extensions.hover.IHoverRequest;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TypesHoverParticipant extends HoverParticipantAdapter {

    private final DayzMissionService missionService;

    public TypesHoverParticipant(DayzMissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public Hover onAttributeValue(IHoverRequest request, CancelChecker cancelChecker) throws Exception {
        var document = request.getXMLDocument();
        if (TypesModel.isTypes(document) && missionService.isInMissionFolder(document)) {
            var userLimitsDefinitions = missionService.getUserFlags();
            var parent = document.findNodeAt(request.getOffset());
            var nodeName = parent.getNodeName();
            var attrName = request.getCurrentAttributeName();

            if (TypesModel.USER_ATTRIBUTE.equals(attrName) && userLimitsDefinitions.containsKey(nodeName)) {
                return hoverForUserFlag(request, userLimitsDefinitions.get(nodeName), cancelChecker);
            }
        }
        return null;
    }

    @Override
    public Hover onText(IHoverRequest request, CancelChecker cancelChecker) throws Exception {
        var document = request.getXMLDocument();
        if (TypesModel.isTypes(document)) {
            var parent = request.getParentElement();
            if (TypesModel.TIME_INTERVAL_TAGS.contains(parent.getNodeName())) {
                return hoverForTimeInterval(request, cancelChecker);
            }
        }
        return null;
    }

    private Hover hoverForUserFlag(IHoverRequest request, Map<String, List<String>> userFlags, CancelChecker cancelChecker) {
        var supportsMarkdown = request.canSupportMarkupKind(MarkupKind.MARKDOWN);
        var attrValue = request.getCurrentAttribute().getValue();

        if (userFlags.containsKey(attrValue) && !userFlags.get(attrValue).isEmpty()) {
            var options = userFlags.get(attrValue);
            var content = supportsMarkdown ? formatAsMarkdown(options) : formatAsText(options);
            return new Hover(content);
        }
        return null;
    }

    private Hover hoverForTimeInterval(IHoverRequest request, CancelChecker cancelChecker) {
        var content = request.getNode().getTextContent();

        return Optional.ofNullable(Ints.tryParse(content))
                .map(TypesHoverParticipant::toReadableTime)
                .map(str -> new Hover(new MarkupContent(MarkupKind.PLAINTEXT, str)))
                .orElse(null);
    }

    private MarkupContent formatAsMarkdown(Iterable<String> options) {
        var content = "* " + String.join("\n* ", options);
        return new MarkupContent(MarkupKind.MARKDOWN, content);
    }

    private MarkupContent formatAsText(Iterable<String> options) {
        var content = String.join(", ", options);
        return new MarkupContent(MarkupKind.PLAINTEXT, content);
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
}

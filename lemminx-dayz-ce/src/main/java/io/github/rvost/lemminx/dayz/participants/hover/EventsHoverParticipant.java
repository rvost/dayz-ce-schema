package io.github.rvost.lemminx.dayz.participants.hover;

import io.github.rvost.lemminx.dayz.model.EventsModel;
import io.github.rvost.lemminx.dayz.participants.ParticipantsUtils;
import org.eclipse.lemminx.services.extensions.hover.HoverParticipantAdapter;
import org.eclipse.lemminx.services.extensions.hover.IHoverRequest;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

public class EventsHoverParticipant extends HoverParticipantAdapter {

    @Override
    public Hover onText(IHoverRequest request, CancelChecker cancelChecker) throws Exception {
        var document = request.getXMLDocument();
        if (EventsModel.isEvents(document)) {
            var parent = request.getParentElement();
            if (EventsModel.TIME_INTERVAL_TAGS.contains(parent.getNodeName())) {
                return ParticipantsUtils.hoverForTimeInterval(request, cancelChecker);
            }
        }
        return null;
    }
}

package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Set;

public class GlobalsModel {
    public static final String GLOBALS_FILE = "globals.xml";
    public static final String VARIABLES_TAG = "variables";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String VALUE_ATTRIBUTE = "value";
    public static final Set<GlobalsVariable> TIME_INTERVAL_VARS = Set.of(
            GlobalsVariable.CleanupLifetimeDeadAnimal,
            GlobalsVariable.CleanupLifetimeDeadInfected,
            GlobalsVariable.CleanupLifetimeDeadPlayer,
            GlobalsVariable.CleanupLifetimeDefault,
            GlobalsVariable.CleanupLifetimeRuined,
            GlobalsVariable.FlagRefreshFrequency,
            GlobalsVariable.FlagRefreshMaxDuration,
            GlobalsVariable.IdleModeCountdown,
            GlobalsVariable.TimeHopping,
            GlobalsVariable.TimeLogin,
            GlobalsVariable.TimeLogout,
            GlobalsVariable.TimePenalty
    );

    public static boolean match(DOMDocument document) {
        var docElement = document.getDocumentElement();
        return docElement != null && VARIABLES_TAG.equals(docElement.getNodeName());
    }
}


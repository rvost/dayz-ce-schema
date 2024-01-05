package io.github.rvost.lemminx.dayz.model;

import org.eclipse.lemminx.dom.DOMDocument;

import java.util.Set;

public class CfgWeatherModel {
    public static final String CFGWEATHER_FILE = "cfgweather.xml";
    public static final String CURRENT_TAG = "current";
    public static final String TIMELIMITS_TAG = "timelimits";
    public static final String DURATION_ATTRIBUTE = "duration";
    public static final String MAX_ATTRIBUTE = "max";
    public static final String MIN_ATTRIBUTE = "min";
    public static final String TIME_ATTRIBUTE = "time";
    public static final Set<String> TIME_INTERVAL_TAGS = Set.of(CURRENT_TAG, TIMELIMITS_TAG);
    public static final Set<String> TIME_INTERVAL_ATTRIBUTES =
            Set.of(DURATION_ATTRIBUTE, MAX_ATTRIBUTE, MIN_ATTRIBUTE, TIME_ATTRIBUTE);


    public static boolean isCfgWeather(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uri = document.getDocumentURI();
        return uri != null && uri.toLowerCase().endsWith(CFGWEATHER_FILE);
    }
}

package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.model.RandomPresetsModel;
import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

public class RandomPresetsModelTests {
    @Test
    public void testEmpty() throws Exception {
        var url = getClass().getClassLoader().getResource("cfgrandompresets/empty.xml");
        var path = Path.of(url.toURI());

        var presets = DocumentUtils.tryParseDocument(path)
                .map(RandomPresetsModel::getRandomPresets)
                .orElse(Map.of());

        Assertions.assertTrue(presets.containsKey("cargo"));
        Assertions.assertTrue(presets.containsKey("attachments"));
        Assertions.assertEquals(0, presets.get("cargo").size());
        Assertions.assertEquals(0, presets.get("attachments").size());
    }

    @Test
    public void testSingleCargoPreset() throws Exception {
        var url = getClass().getClassLoader().getResource("cfgrandompresets/singleCargo.xml");
        var path = Path.of(url.toURI());

        var presets = DocumentUtils.tryParseDocument(path)
                .map(RandomPresetsModel::getRandomPresets)
                .orElse(Map.of());

        Assertions.assertTrue(presets.containsKey("cargo"));
        Assertions.assertTrue(presets.containsKey("attachments"));
        Assertions.assertEquals(1, presets.get("cargo").size());
        Assertions.assertEquals(0, presets.get("attachments").size());
    }

    @Test
    public void testSingleAttachmentsPreset() throws Exception {
        var url = getClass().getClassLoader().getResource("cfgrandompresets/singleAttachments.xml");
        var path = Path.of(url.toURI());

        var presets = DocumentUtils.tryParseDocument(path)
                .map(RandomPresetsModel::getRandomPresets)
                .orElse(Map.of());

        Assertions.assertTrue(presets.containsKey("cargo"));
        Assertions.assertTrue(presets.containsKey("attachments"));
        Assertions.assertEquals(0, presets.get("cargo").size());
        Assertions.assertEquals(1, presets.get("attachments").size());
    }

    @Test
    public void testMultiplePresets() throws Exception {
        var url = getClass().getClassLoader().getResource("cfgrandompresets/multiplePresets.xml");
        var path = Path.of(url.toURI());

        var presets = DocumentUtils.tryParseDocument(path)
                .map(RandomPresetsModel::getRandomPresets)
                .orElse(Map.of());

        Assertions.assertEquals(3, presets.get("cargo").size());
        Assertions.assertEquals(4, presets.get("attachments").size());
    }
}

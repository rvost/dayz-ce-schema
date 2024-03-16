package io.github.rvost.lemminx.dayz;

import com.google.common.collect.ImmutableBiMap;
import io.github.rvost.lemminx.dayz.model.LimitsDefinitionUserModel;
import io.github.rvost.lemminx.dayz.utils.DocumentUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

public class LimitsDefinitionUserModelTests {

    @Test
    public void testEmptyCfgLimitsDefinitionsUser() throws Exception {
        var url = getClass().getClassLoader().getResource("cfglimitsdefinitionuser/empty.xml");
        var path = Path.of(url.toURI());

        var flags = DocumentUtils.tryParseDocument(path)
                .map(LimitsDefinitionUserModel::getUserLimitsDefinitions)
                .orElse(Map.of());

        Assertions.assertEquals(0, flags.get("usage").size());
        Assertions.assertEquals(0, flags.get("value").size());
    }

    @Test
    public void testSimpleCfgLimitsDefinitionsUser() throws Exception {
        var url = getClass().getClassLoader().getResource("cfglimitsdefinitionuser/simple.xml");
        var path = Path.of(url.toURI());

        var flags = DocumentUtils.tryParseDocument(path)
                .map(LimitsDefinitionUserModel::getUserLimitsDefinitions)
                .orElse(Map.of());

        Assertions.assertEquals(2, flags.size());
        Assertions.assertTrue(flags.containsKey("usage"));
        Assertions.assertTrue(flags.get("usage").contains("TownVillage"));
        Assertions.assertTrue(flags.containsKey("value"));
        Assertions.assertTrue(flags.get("value").contains("Tier12"));
    }

    @Test
    public void testSimpleUserFlagDefinitions() throws Exception {
        var url = getClass().getClassLoader().getResource("cfglimitsdefinitionuser/simple.xml");
        var path = Path.of(url.toURI());

        var flags = DocumentUtils.tryParseDocument(path)
                .map(LimitsDefinitionUserModel::getUserFlags)
                .orElse(ImmutableBiMap.of());

        Assertions.assertEquals(2, flags.size());
        Assertions.assertTrue(flags.containsKey("TownVillage"));
        Assertions.assertEquals(2, flags.get("TownVillage").size());
    }
}

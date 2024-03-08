package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.model.LimitsDefinitionUserModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LimitsDefinitionsModelTests {

    @Test
    public void testEmptyCfgLimitsDefinitionsUser() throws Exception {
        var input = getClass().getClassLoader().getResourceAsStream("cfglimitsdefinitionuser/empty.xml");
        var flags = LimitsDefinitionUserModel.getUserLimitsDefinitions(input);
        Assertions.assertEquals(0, flags.size());
        input.close();
    }

    @Test
    public void testSimpleCfgLimitsDefinitionsUser() throws Exception {
        var input = getClass().getClassLoader().getResourceAsStream("cfglimitsdefinitionuser/simple.xml");
        var flags = LimitsDefinitionUserModel.getUserLimitsDefinitions(input);
        Assertions.assertEquals(2, flags.size());
        Assertions.assertTrue(flags.containsKey("usage"));
        Assertions.assertTrue(flags.get("usage").contains("TownVillage"));
        Assertions.assertTrue(flags.containsKey("value"));
        Assertions.assertTrue(flags.get("value").contains("Tier12"));
        input.close();
    }

    @Test
    public void testSimpleUserFlagDefinitions() throws Exception {
        var input = getClass().getClassLoader().getResourceAsStream("cfglimitsdefinitionuser/simple.xml");
        var flags = LimitsDefinitionUserModel.getUserFlags(input);

        Assertions.assertEquals(2, flags.size());
        Assertions.assertTrue(flags.containsKey("TownVillage"));
        Assertions.assertEquals(2, flags.get("TownVillage").size());
    }
}

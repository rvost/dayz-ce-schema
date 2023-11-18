package io.github.rvost.lemminx.dayz;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DayzMissionServiceTests {

    @Test
    public void testEmptyCfgLimitsDefinitionsUser() throws Exception {
        var input = getClass().getClassLoader().getResourceAsStream("cfglimitsdefinitionuser/empty.xml");
        var flags = DayzMissionService.getUserLimitsDefinitions(input);
        Assertions.assertEquals(0, flags.size());
        input.close();
    }

    @Test
    public void testSimpleCfgLimitsDefinitionsUser() throws Exception {
        var input = getClass().getClassLoader().getResourceAsStream("cfglimitsdefinitionuser/simple.xml");
        var flags = DayzMissionService.getUserLimitsDefinitions(input);
        Assertions.assertEquals(2, flags.size());
        Assertions.assertTrue(flags.containsKey("usage"));
        Assertions.assertTrue(flags.get("usage").contains("TownVillage"));
        Assertions.assertTrue(flags.containsKey("value"));
        Assertions.assertTrue(flags.get("value").contains("Tier12"));
        input.close();
    }
}

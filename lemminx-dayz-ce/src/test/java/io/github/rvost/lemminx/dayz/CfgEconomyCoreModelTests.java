package io.github.rvost.lemminx.dayz;

import io.github.rvost.lemminx.dayz.model.CfgEconomyCoreModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class CfgEconomyCoreModelTests {
    @Test
    public void testEmptyCESection() throws Exception {
        var missionPath = Path.of("P:\\mission");
        var fileURL = getClass().getClassLoader().getResource("cfgeconomycore/empty.xml");
        var path = Path.of(fileURL.toURI());

        var files = CfgEconomyCoreModel.getCustomFilesFromFile(path, missionPath);

        Assertions.assertTrue(files.isEmpty());
    }

    @Test
    public void testSingleFile() throws Exception {
        var missionPath = Path.of("P:\\mission");
        var fileURL = getClass().getClassLoader().getResource("cfgeconomycore/simple.xml");
        var path = Path.of(fileURL.toURI());
        var expectedKey = missionPath.resolve("db").resolve("types_dzn.xml").toAbsolutePath();
        var files = CfgEconomyCoreModel.getCustomFilesFromFile(path, missionPath);

        Assertions.assertEquals(1, files.size());
        Assertions.assertTrue(files.containsKey(expectedKey));
    }
}

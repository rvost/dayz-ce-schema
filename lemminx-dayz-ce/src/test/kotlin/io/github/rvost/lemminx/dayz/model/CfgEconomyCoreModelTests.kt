package io.github.rvost.lemminx.dayz.model

import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CfgEconomyCoreModelTests {
    @Test
    fun testEmptyCESection() {
        val missionPath = Path.of("P:\\mission")
        val fileURL = javaClass.classLoader.getResource("cfgeconomycore/empty.xml")
        val path = Path.of(fileURL.toURI())

        val files = CfgEconomyCoreModel.getCustomFilesFromFile(path, missionPath)

        assertTrue(files.isEmpty())
    }

    @Test
    fun testSingleFile() {
        val missionPath = Path.of("P:\\mission")
        val fileURL = javaClass.classLoader.getResource("cfgeconomycore/simple.xml")
        val path = Path.of(fileURL.toURI())
        val expectedKey = missionPath.resolve("db").resolve("types_dzn.xml").toAbsolutePath()
        val files = CfgEconomyCoreModel.getCustomFilesFromFile(path, missionPath)

        assertEquals(1, files.size)
        assertTrue(files.containsKey(expectedKey))
    }
}

package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lemminx.dom.DOMDocument
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LimitsDefinitionUserModelTests {
    @Test
    fun testEmptyCfgLimitsDefinitionsUser() {
        val url = javaClass.classLoader.getResource("cfglimitsdefinitionuser/empty.xml")
        val path = Path.of(url.toURI())

        val flags = DocumentUtils.tryParseDocument(path)
            .map { doc: DOMDocument? -> LimitsDefinitionUserModel.getUserLimitsDefinitions(doc) }
            .orElseThrow()

        assertEquals(0, flags["usage"]!!.size)
        assertEquals(0, flags["value"]!!.size)
    }

    @Test
    fun testSimpleCfgLimitsDefinitionsUser() {
        val url = javaClass.classLoader.getResource("cfglimitsdefinitionuser/simple.xml")
        val path = Path.of(url.toURI())

        val flags = DocumentUtils.tryParseDocument(path)
            .map { doc: DOMDocument? -> LimitsDefinitionUserModel.getUserLimitsDefinitions(doc) }
            .orElseThrow()

        assertEquals(2, flags.size)
        assertTrue(flags["usage"]!!.contains("TownVillage"))
        assertTrue(flags["value"]!!.contains("Tier12"))
    }

    @Test
    fun testSimpleUserFlagDefinitions() {
        val url = javaClass.classLoader.getResource("cfglimitsdefinitionuser/simple.xml")
        val path = Path.of(url.toURI())

        val flags = DocumentUtils.tryParseDocument(path)
            .map { doc: DOMDocument? -> LimitsDefinitionUserModel.getUserFlags(doc) }
            .orElseThrow()

        assertEquals(2, flags.size)
        assertTrue(flags.contains("TownVillage"))
        assertEquals(2, flags["TownVillage"]!!.size)
    }
}

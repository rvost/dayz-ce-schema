package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals

class LimitsDefinitionModelTests {

    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("cfglimitsdefinition/empty.xml")
        val path = Path.of(url.toURI())

        val definitions = DocumentUtils.tryParseDocument(path)
            .map { doc -> LimitsDefinitionModel.getLimitsDefinitions(doc) }
            .orElseThrow()

        assertEquals(0, definitions["category"]!!.size)
        assertEquals(0, definitions["tag"]!!.size)
        assertEquals(0, definitions["usage"]!!.size)
        assertEquals(0, definitions["value"]!!.size)
    }

    @Test
    fun testEmptyUserFlags() {
        val url = javaClass.classLoader.getResource("cfglimitsdefinition/emptyUserFlags.xml")
        val path = Path.of(url.toURI())

        val definitions = DocumentUtils.tryParseDocument(path)
            .map { doc -> LimitsDefinitionModel.getLimitsDefinitions(doc) }
            .orElseThrow()

        assertEquals(setOf("tools", "containers"), definitions["category"])
        assertEquals(setOf("floor", "ground"), definitions["tag"])
        assertEquals(0, definitions["usage"]!!.size)
        assertEquals(0, definitions["value"]!!.size)
    }

    @Test
    fun testSimple() {
        val url = javaClass.classLoader.getResource("cfglimitsdefinition/simple.xml")
        val path = Path.of(url.toURI())

        val definitions = DocumentUtils.tryParseDocument(path)
            .map { doc -> LimitsDefinitionModel.getLimitsDefinitions(doc) }
            .orElseThrow()

        assertEquals(setOf("tools", "containers"), definitions["category"])
        assertEquals(setOf("floor", "ground"), definitions["tag"])
        assertEquals(setOf("Military", "Police"), definitions["usage"])
        assertEquals(setOf("Tier1", "Tier2"), definitions["value"])
    }
}
package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypesModelTests {
    @Test
    fun testMatchAnyFileName() {
        val url = javaClass.classLoader.getResource("types/simple.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertTrue { TypesModel.match(doc) }
    }

    @Test
    fun testNotMatchIncompatibleSchema() {
        val url = javaClass.classLoader.getResource("spawnabletypes/simple.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertFalse { TypesModel.match(doc) }
    }

    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("types/empty.xml")
        val path = Path.of(url.toURI())

        val types = TypesModel.getTypes(path)

        assertTrue { types.isEmpty() }
    }

    @Test
    fun testSimple() {
        val url = javaClass.classLoader.getResource("types/simple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "ACOGOptic" to Range(Position(2, 4), Position(13, 11)),
            "ACOGOptic_6x" to Range(Position(14, 4), Position(28, 11))
        )

        val types = TypesModel.getTypes(path)

        assertEquals(expected, types)
    }

    @Test
    fun testDuplicatesLastWins() {
        val url = javaClass.classLoader.getResource("types/duplicates.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "ACOGOptic" to Range(Position(29, 4), Position(40, 11)),
            "ACOGOptic_6x" to Range(Position(14, 4), Position(28, 11))
        )

        val types = TypesModel.getTypes(path)

        assertEquals(expected, types)
    }

    @Test
    fun testFlagIndexEmpty() {
        val url = javaClass.classLoader.getResource("types/empty.xml")
        val path = Path.of(url.toURI())

        val index = TypesModel.getFlagIndex(path)

        assertTrue { index.isEmpty() }
    }

    @Test
    fun testFlagIndexSimple() {
        val url = javaClass.classLoader.getResource("types/simple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "weapons" to listOf(
                Range(Position(11, 8), Position(11, 34)),
                Range(Position(23, 8), Position(23, 34)),
            ),
            "Military" to listOf(
                Range(Position(12, 8), Position(12, 32)),
                Range(Position(24, 8), Position(24, 32)),
            ),
            "Police" to listOf(Range(Position(25, 8), Position(25, 30))),
            "Tier3" to listOf(Range(Position(26, 8), Position(26, 29))),
            "Tier4" to listOf(Range(Position(27, 8), Position(27, 29))),
        )

        val index = TypesModel.getFlagIndex(path)

        assertEquals(expected, index)
    }

    @Test
    fun testUserFlagIndexEmpty() {
        val url = javaClass.classLoader.getResource("types/empty.xml")
        val path = Path.of(url.toURI())

        val index = TypesModel.getUserFlagIndex(path)

        assertTrue { index.isEmpty() }
    }

    @Test
    fun testUserFlagIndexSimple() {
        val url = javaClass.classLoader.getResource("types/userFlagsSimple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "Tier34" to listOf(Range(Position(13, 8), Position(13, 30))),
            "PoliceMilitary" to listOf(Range(Position(25, 8), Position(25, 38)))
        )

        val index = TypesModel.getUserFlagIndex(path)

        assertEquals(expected, index)
    }
}
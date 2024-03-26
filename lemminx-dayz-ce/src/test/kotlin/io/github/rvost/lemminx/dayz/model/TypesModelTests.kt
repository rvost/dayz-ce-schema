package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lemminx.XMLAssert.r
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
            "ACOGOptic" to r(2, 4, 13, 11),
            "ACOGOptic_6x" to r(14, 4, 28, 11),
        )

        val types = TypesModel.getTypes(path)

        assertEquals(expected, types)
    }

    @Test
    fun testDuplicatesLastWins() {
        val url = javaClass.classLoader.getResource("types/duplicates.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "ACOGOptic" to r(29, 4, 40, 11),
            "ACOGOptic_6x" to r(14, 4, 28, 11),
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
                r(11, 8, 34),
                r(23, 8, 34),
            ),
            "Military" to listOf(
                r(12, 8, 32),
                r(24, 8, 32),
            ),
            "Police" to listOf(r(25, 8, 30)),
            "Tier3" to listOf(r(26,8,29)),
            "Tier4" to listOf(r(27,8,29)),
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
            "Tier34" to listOf(r(13,8,30)),
            "PoliceMilitary" to listOf(r(25,8,38)),
        )

        val index = TypesModel.getUserFlagIndex(path)

        assertEquals(expected, index)
    }
}
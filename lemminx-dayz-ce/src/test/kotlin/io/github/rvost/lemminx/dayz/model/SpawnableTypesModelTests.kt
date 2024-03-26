package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lemminx.XMLAssert.r
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpawnableTypesModelTests {
    @Test
    fun testMatchAnyFileName() {
        val url = javaClass.classLoader.getResource("spawnabletypes/simple.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertTrue { SpawnableTypesModel.match(doc) }
    }

    @Test
    fun testNotMatchIncompatibleSchema() {
        val url = javaClass.classLoader.getResource("types/simple.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertFalse { SpawnableTypesModel.match(doc) }
    }

    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("spawnabletypes/empty.xml")
        val path = Path.of(url.toURI())

        val types = SpawnableTypesModel.getSpawnableTypes(path)

        assertTrue { types.isEmpty() }
    }

    @Test
    fun testSimple() {
        val url = javaClass.classLoader.getResource("spawnabletypes/simple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "WoodenCrate" to r(2, 4, 4, 11),
            "GiftWrapPaper" to r(5, 4, 7, 11),
        )

        val types = SpawnableTypesModel.getSpawnableTypes(path)

        assertEquals(expected, types)
    }

    @Test
    fun testDuplicatesLastWins() {
        val url = javaClass.classLoader.getResource("spawnabletypes/duplicates.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "WoodenCrate" to r(8, 4, 9, 11),
            "GiftWrapPaper" to r(5, 4, 7, 11),
        )

        val types = SpawnableTypesModel.getSpawnableTypes(path)

        assertEquals(expected, types)
    }

    @Test
    fun testPresetIndexEmpty() {
        val url = javaClass.classLoader.getResource("spawnabletypes/simple.xml")
        val path = Path.of(url.toURI())

        val index = SpawnableTypesModel.getPresetsIndex(path)

        assertTrue { index.isEmpty() }
    }

    @Test
    fun testPresetIndexSimple() {
        val url = javaClass.classLoader.getResource("spawnabletypes/presetsSimple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "foodVillage" to listOf(r(3, 8, 38)),
            "mixVillage" to listOf(r(4, 8, 37)),
            "mixArmy" to listOf(
                r(7, 8, 34),
                r(8, 8, 34),
                r(9, 8, 34),
            )
        )

        val index = SpawnableTypesModel.getPresetsIndex(path)

        assertEquals(expected, index)
    }

}
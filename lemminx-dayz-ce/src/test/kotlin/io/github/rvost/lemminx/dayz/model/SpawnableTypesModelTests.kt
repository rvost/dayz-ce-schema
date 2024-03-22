package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
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
            "WoodenCrate" to Range(Position(2, 4), Position(4, 11)),
            "GiftWrapPaper" to Range(Position(5, 4), Position(7, 11))
        )

        val types = SpawnableTypesModel.getSpawnableTypes(path)

        assertEquals(expected, types)
    }

    @Test
    fun testDuplicatesLastWins() {
        val url = javaClass.classLoader.getResource("spawnabletypes/duplicates.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "WoodenCrate" to Range(Position(8, 4), Position(9, 11)),
            "GiftWrapPaper" to Range(Position(5, 4), Position(7, 11))
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
            "foodVillage" to listOf(Range(Position(3, 8), Position(3, 38))),
            "mixVillage" to listOf(Range(Position(4, 8), Position(4, 37))),
            "mixArmy" to listOf(
                Range(Position(7, 8), Position(7, 34)),
                Range(Position(8, 8), Position(8, 34)),
                Range(Position(9, 8), Position(9, 34))
            )
        )

        val index = SpawnableTypesModel.getPresetsIndex(path)

        assertEquals(expected, index)
    }

}
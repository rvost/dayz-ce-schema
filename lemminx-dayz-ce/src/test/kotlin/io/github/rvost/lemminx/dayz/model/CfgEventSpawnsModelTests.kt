package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CfgEventSpawnsModelTests {
    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("cfgeventspawns/empty.xml")
        val path = Path.of(url.toURI())

        val spawns = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventSpawnsModel.getCfgEventSpawns(doc) }
            .orElseThrow()

        assertTrue(spawns.isEmpty())
    }

    @Test
    fun testSimple() {
        val url = javaClass.classLoader.getResource("cfgeventspawns/simple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "VehicleVodnik" to Range(Position(2, 4), Position(7, 12)),
            "AnimalRoeDeer" to Range(Position(8, 4), Position(8, 34)),
            "AnimalWolf" to Range(Position(9, 4), Position(9, 31)),
            "AnimalWildBoar" to Range(Position(10, 4), Position(10, 35))
        )

        val spawns = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventSpawnsModel.getCfgEventSpawns(doc) }
            .orElseThrow()

        assertEquals(expected, spawns)
    }

    @Test
    fun testGroupReferencesEmpty() {
        val url = javaClass.classLoader.getResource("cfgeventspawns/empty.xml")
        val path = Path.of(url.toURI())

        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventSpawnsModel.getEventSpawnGroupReferences(doc) }
            .orElseThrow()

        assertTrue(groups.isEmpty())
    }

    @Test
    fun testGroupReferencesUnique() {
        val url = javaClass.classLoader.getResource("cfgeventspawns/groupsUnique.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "Block_Dolina" to Range(Position(10, 8), Position(10, 80)),
            "Block_Elektro" to Range(Position(11, 8), Position(11, 80))
        )

        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventSpawnsModel.getEventSpawnGroupReferences(doc) }
            .orElseThrow()

        assertEquals(expected, groups)
    }

    @Test
    fun testGroupReferencesDuplicatesFirstWin() {
        val url = javaClass.classLoader.getResource("cfgeventspawns/groupsDuplicates.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "Block_Dolina" to Range(Position(4, 8), Position(4, 80)),
            "Block_Elektro" to Range(Position(5, 8), Position(5, 80))
        )

        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventSpawnsModel.getEventSpawnGroupReferences(doc) }
            .orElseThrow()

        assertEquals(expected, groups)
    }
}
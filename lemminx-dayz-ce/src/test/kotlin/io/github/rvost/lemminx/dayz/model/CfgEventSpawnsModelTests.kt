package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lemminx.XMLAssert.r
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
            "VehicleVodnik" to r(2, 4, 7, 12),
            "AnimalRoeDeer" to r(8, 4, 34),
            "AnimalWolf" to r(9, 4, 31),
            "AnimalWildBoar" to r(10, 4, 35),
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
            "Block_Dolina" to r(10, 8, 80),
            "Block_Elektro" to r(11, 8, 80),
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
            "Block_Dolina" to r(4, 8, 80),
            "Block_Elektro" to r(5, 8, 80),
        )

        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventSpawnsModel.getEventSpawnGroupReferences(doc) }
            .orElseThrow()

        assertEquals(expected, groups)
    }
}
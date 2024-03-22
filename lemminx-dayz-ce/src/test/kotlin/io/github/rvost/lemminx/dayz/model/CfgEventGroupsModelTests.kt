package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CfgEventGroupsModelTests {
    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("cfgeventgroups/empty.xml")
        val path = Path.of(url.toURI())

        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventGroupsModel.getCfgEventGroups(doc) }
            .orElseThrow()

        assertTrue { groups.isEmpty() }
    }

    @Test
    fun testSimple() {
        val url = javaClass.classLoader.getResource("cfgeventgroups/simple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "Train_Abandoned_Cherno" to Range(Position(4, 4), Position(19, 12)),
            "Train_Abandoned_Kamarovo" to Range(Position(22, 4), Position(53, 12)),
            "Train_Abandoned_Kamy" to Range(Position(56, 4), Position(75, 12))
        )


        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventGroupsModel.getCfgEventGroups(doc) }
            .orElseThrow()

        assertEquals(expected, groups)
    }

    @Test
    fun testTypesIndexEmpty() {
        val url = javaClass.classLoader.getResource("cfgeventgroups/empty.xml")
        val path = Path.of(url.toURI())

        val index = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventGroupsModel.getChildTypesIndex(doc) }
            .orElseThrow()

        assertTrue(index.isEmpty())
    }

    @Test
    fun testTypesIndexUniqueValues() {
        val url = javaClass.classLoader.getResource("cfgeventgroups/typesIndex.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "StaticObj_Wreck_Train_742_Red_DE" to listOf(Range(Position(5, 8), Position(6, 41))),
            "StaticObj_Wreck_Train_Wagon_Tanker_DE" to listOf(Range(Position(7, 8), Position(8, 59))),
            "Land_Train_Wagon_Box_DE" to listOf(Range(Position(9, 8), Position(10, 49))),
        )

        val index = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventGroupsModel.getChildTypesIndex(doc) }
            .orElseThrow()

        assertEquals(expected, index)
    }

    @Test
    fun testTypesIndexDuplicateValues() {
        val url = javaClass.classLoader.getResource("cfgeventgroups/typesIndexDuplicates.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "StaticObj_Wreck_Train_742_Red_DE" to listOf(Range(Position(5, 8), Position(6, 41))),
            "Land_Train_Wagon_Box_DE" to listOf(
                Range(Position(7, 8), Position(8, 47)),
                Range(Position(9, 8), Position(10, 49))
            )
        )

        val index = DocumentUtils.tryParseDocument(path)
            .map { doc -> CfgEventGroupsModel.getChildTypesIndex(doc) }
            .orElseThrow()

        assertEquals(expected, index)
    }
}
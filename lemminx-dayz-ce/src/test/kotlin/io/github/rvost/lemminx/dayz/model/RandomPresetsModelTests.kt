package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lemminx.XMLAssert.r
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RandomPresetsModelTests {
    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("cfgrandompresets/empty.xml")
        val path = Path.of(url.toURI())

        val presets = DocumentUtils.tryParseDocument(path)
            .map { doc -> RandomPresetsModel.getRandomPresets(doc) }
            .orElse(mapOf())

        assertTrue(presets.containsKey("cargo"))
        assertTrue(presets.containsKey("attachments"))
        assertEquals(0, presets["cargo"]!!.size)
        assertEquals(0, presets["attachments"]!!.size)
    }

    @Test
    fun testSingleCargoPreset() {
        val url = javaClass.classLoader.getResource("cfgrandompresets/singleCargo.xml")
        val path = Path.of(url.toURI())

        val presets = DocumentUtils.tryParseDocument(path)
            .map { doc -> RandomPresetsModel.getRandomPresets(doc) }
            .orElseThrow()

        assertTrue(presets.containsKey("cargo"))
        assertTrue(presets.containsKey("attachments"))
        assertEquals(1, presets["cargo"]!!.size)
        assertEquals(0, presets["attachments"]!!.size)
    }

    @Test
    fun testSingleAttachmentsPreset() {
        val url = javaClass.classLoader.getResource("cfgrandompresets/singleAttachments.xml")
        val path = Path.of(url.toURI())

        val presets = DocumentUtils.tryParseDocument(path)
            .map { doc -> RandomPresetsModel.getRandomPresets(doc) }
            .orElseThrow()

        assertTrue(presets.containsKey("cargo"))
        assertTrue(presets.containsKey("attachments"))
        assertEquals(0, presets["cargo"]!!.size)
        assertEquals(1, presets["attachments"]!!.size)
    }

    @Test
    fun testMultiplePresets() {
        val url = javaClass.classLoader.getResource("cfgrandompresets/multiplePresets.xml")
        val path = Path.of(url.toURI())

        val presets = DocumentUtils.tryParseDocument(path)
            .map { doc -> RandomPresetsModel.getRandomPresets(doc) }
            .orElseThrow()

        assertEquals(3, presets["cargo"]!!.size)
        assertEquals(4, presets["attachments"]!!.size)
    }

    @Test
    fun testIndexEmpty() {
        val url = javaClass.classLoader.getResource("cfgrandompresets/empty.xml")
        val path = Path.of(url.toURI())

        val index = DocumentUtils.tryParseDocument(path)
            .map { doc -> RandomPresetsModel.getRandomPresetsIndex(doc) }
            .orElseThrow()

        assertTrue { index.isEmpty() }
    }

    @Test
    fun testIndexSimple() {
        val url = javaClass.classLoader.getResource("cfgrandompresets/multiplePresets.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "mixArmy" to r(2, 4, 19, 12),
            "grenades" to r(20, 4, 25, 12),
            "optics" to r(26, 4, 32, 18),
            "ContaminatedCargo" to r(34, 4, 36, 12),
            "hatsFarm" to r(38, 4, 49, 18),
            "bagsHunter" to r(50, 4, 55, 18),
            "vestsHunter" to r(56, 4, 59, 18),
        )

        val index = DocumentUtils.tryParseDocument(path)
            .map { doc -> RandomPresetsModel.getRandomPresetsIndex(doc) }
            .orElseThrow()

        assertEquals(expected, index)
    }
}

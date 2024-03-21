package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lemminx.dom.DOMDocument
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
            .map { doc: DOMDocument? -> RandomPresetsModel.getRandomPresets(doc) }
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
            .map { doc: DOMDocument? -> RandomPresetsModel.getRandomPresets(doc) }
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
            .map { doc: DOMDocument? -> RandomPresetsModel.getRandomPresets(doc) }
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
            .map { doc: DOMDocument? -> RandomPresetsModel.getRandomPresets(doc) }
            .orElseThrow()

        assertEquals(3, presets["cargo"]!!.size)
        assertEquals(4, presets["attachments"]!!.size)
    }
}

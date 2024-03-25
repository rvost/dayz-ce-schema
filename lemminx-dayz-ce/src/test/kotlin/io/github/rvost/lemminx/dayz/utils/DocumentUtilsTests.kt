package io.github.rvost.lemminx.dayz.utils

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentUtilsTests {
    @Test
    fun `tryParseDocument returns empty on IO error`() {
        val path = Path.of("P://not exist")

        val result = DocumentUtils.tryParseDocument(path)

        assertTrue { result.isEmpty }
    }

    @Test
    fun `tryParseDocument returns DOMDocument for empty file`() {
        val url = javaClass.classLoader.getResource("documentutils/empty_file.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)

        assertTrue { result.isPresent }
    }

    @Test
    fun `tryParseDocument returns DOMDocument for text file`() {
        val url = javaClass.classLoader.getResource("documentutils/text.txt")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)

        assertTrue { result.isPresent }
    }

    @Test
    fun `tryParseDocument returns DOMDocument for XML file`() {
        val url = javaClass.classLoader.getResource("documentutils/empty_document.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)

        assertTrue { result.isPresent }
    }

    @Test
    fun `tryParseDocument returns DOMDocument for malformed XML file`() {
        val url = javaClass.classLoader.getResource("documentutils/empty_document.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)

        assertTrue { result.isPresent }
    }

    @Test
    fun `indexByAttribute returns empty map for empty XML file`() {
        val url = javaClass.classLoader.getResource("documentutils/empty_document.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertTrue { result.isEmpty() }
    }

    @Test
    fun `indexByAttribute returns single result`() {
        val url = javaClass.classLoader.getResource("documentutils/index_single.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(1, result.size)
    }

    @Test
    fun `indexByAttribute selects whole tag`() {
        val url = javaClass.classLoader.getResource("documentutils/index_single.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "b" to Range(Position(2, 4), Position(2, 20))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexByAttribute selects latest occurrence`() {
        val url = javaClass.classLoader.getResource("documentutils/index_multiple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "b" to Range(Position(4, 4), Position(4, 20))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexByAttribute resilient to missing closing document tag`() {
        val url = javaClass.classLoader.getResource("documentutils/index_no_closing_document_tag.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "b" to Range(Position(2, 4), Position(2, 20)),
            "c" to Range(Position(3, 4), Position(3, 20))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexByAttribute resilient to malformed elements`() {
        val url = javaClass.classLoader.getResource("documentutils/index_malformed_index_element.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "b" to Range(Position(2, 4), Position(4, 8)),
            "c" to Range(Position(5, 4), Position(5, 20))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexByAttribute resilient to empty attributes`() {
        val url = javaClass.classLoader.getResource("documentutils/index_empty_attribute.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "b" to Range(Position(2, 4), Position(2, 20)),
            "c" to Range(Position(3, 4), Position(3, 20))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexByAttribute resilient to empty attribute values`() {
        val url = javaClass.classLoader.getResource("documentutils/index_empty_attribute_value.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "b" to Range(Position(2, 4), Position(2, 20)),
            "c" to Range(Position(3, 4), Position(3, 20))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexChildrenByAttribute returns empty map for empty XML file`() {
        val url = javaClass.classLoader.getResource("documentutils/empty_document.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertTrue { result.isEmpty() }
    }

    @Test
    fun `indexChildrenByAttribute returns empty map if no child elements`() {
        val url = javaClass.classLoader.getResource("documentutils/index_single.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertTrue { result.isEmpty() }
    }

    @Test
    fun `indexChildrenByAttribute returns single result`() {
        val url = javaClass.classLoader.getResource("documentutils/index_children_single.xml")
        val path = Path.of(url.toURI())

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(1, result.size)
    }

    @Test
    fun `indexChildrenByAttribute selects whole tag`() {
        val url = javaClass.classLoader.getResource("documentutils/index_children_single.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "c" to listOf(Range(Position(4, 8), Position(4, 24)))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexChildrenByAttribute returns all occurrences`() {
        val url = javaClass.classLoader.getResource("documentutils/index_children_single_key_values.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "c" to listOf(
                Range(Position(3, 8), Position(3, 24)),
                Range(Position(6, 8), Position(6, 24))
            )
        )
        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexChildrenByAttribute resilient to missing closing document tag`() {
        val url = javaClass.classLoader.getResource("documentutils/index_children_no_closing_tag.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "c" to listOf(Range(Position(4, 8), Position(4, 24)))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexChildrenByAttribute resilient to malformed elements`() {
        val url = javaClass.classLoader.getResource("documentutils/index_children_malformed_elements.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "c" to listOf(Range(Position(4, 8), Position(4, 24)))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexChildrenByAttribute resilient to empty attributes`() {
        val url = javaClass.classLoader.getResource("documentutils/index_children_empty_attribute.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "c" to listOf(Range(Position(6, 8), Position(6, 24)))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }

    @Test
    fun `indexChildrenByAttribute resilient to empty attributes values`() {
        val url = javaClass.classLoader.getResource("documentutils/index_children_empty_attribute_value.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "c" to listOf(Range(Position(6, 8), Position(6, 24)))
        )

        val result = DocumentUtils.tryParseDocument(path)
            .map { doc -> DocumentUtils.indexChildrenByAttribute(doc, "name") }
            .orElseThrow()

        assertEquals(expected, result)
    }
}
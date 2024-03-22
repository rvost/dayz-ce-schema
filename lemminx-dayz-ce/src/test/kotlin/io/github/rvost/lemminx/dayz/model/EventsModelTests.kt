package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EventsModelTests {
    @Test
    fun testMatchAnyFileName() {
        val url = javaClass.classLoader.getResource("events/simple.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertTrue { EventsModel.match(doc) }
    }

    @Test
    fun testNotMatchIncompatibleScheme() {
        val url = javaClass.classLoader.getResource("events/notEvents.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertFalse { EventsModel.match(doc) }
    }

    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("events/empty.xml")
        val path = Path.of(url.toURI())

        val events = DocumentUtils.tryParseDocument(path)
            .map { doc -> EventsModel.getEvents(doc) }
            .orElseThrow()

        assertTrue { events.isEmpty() }
    }

    @Test
    fun testSimple() {
        val url = javaClass.classLoader.getResource("events/simple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "AmbientFox" to Range(Position(2, 4), Position(18, 12)),
            "AmbientHare" to Range(Position(19, 4), Position(35, 12))
        )

        val events = DocumentUtils.tryParseDocument(path)
            .map { doc -> EventsModel.getEvents(doc) }
            .orElseThrow()

        assertEquals(expected, events)
    }

    @Test
    fun testDuplicatesLastWins() {
        val url = javaClass.classLoader.getResource("events/duplicates.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "AmbientFox" to Range(Position(36, 4), Position(52, 12)),
            "AmbientHare" to Range(Position(19, 4), Position(35, 12))
        )

        val events = DocumentUtils.tryParseDocument(path)
            .map { doc -> EventsModel.getEvents(doc) }
            .orElseThrow()

        assertEquals(expected, events)
    }
}
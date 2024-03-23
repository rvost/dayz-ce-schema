package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MissionModelTest {
    @ParameterizedTest
    @MethodSource("generateArgs")
    fun testActualCustomFiles(args: Pair<String, DayzFileType>) {
        val (resourcePath, _) = args
        val url = javaClass.classLoader.getResource(resourcePath)
        val path = Path.of(url.toURI())
        val doc = DocumentUtils.tryParseDocument(path).orElseThrow()

        assertTrue { MissionModel.IsCustomFile(doc) }
    }

    @Test
    fun testNotCustomFile() {
        val url = javaClass.classLoader.getResource("mission/cfgignorelist.xml")
        val path = Path.of(url.toURI())
        val doc = DocumentUtils.tryParseDocument(path).orElseThrow()

        assertFalse { MissionModel.IsCustomFile(doc) }
    }

    @Test
    fun testTypeForNotCustomFileIsEmpty() {
        val url = javaClass.classLoader.getResource("mission/cfgignorelist.xml")
        val path = Path.of(url.toURI())
        val doc = DocumentUtils.tryParseDocument(path).orElseThrow()

        assertTrue { MissionModel.TryGetFileType(doc).isEmpty }
    }

    @ParameterizedTest
    @MethodSource("generateArgs")
    fun testForCustomFilesReturnsType(args: Pair<String, DayzFileType>) {
        val (resourcePath, expected) = args
        val url = javaClass.classLoader.getResource(resourcePath)
        val path = Path.of(url.toURI())
        val doc = DocumentUtils.tryParseDocument(path).orElseThrow()

        val result = MissionModel.TryGetFileType(doc).orElseThrow()

        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        fun generateArgs() = listOf(
            "mission/cfgspawnabletypes.xml" to DayzFileType.SPAWNABLETYPES,
            "mission/economy.xml" to DayzFileType.ECONOMY,
            "mission/events.xml" to DayzFileType.EVENTS,
            "mission/globals.xml" to DayzFileType.GLOBALS,
            "mission/messages.xml" to DayzFileType.MESSAGES,
            "mission/types.xml" to DayzFileType.TYPES,
        )
    }
}
package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.eclipse.lemminx.XMLAssert.r
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapGroupProtoModelTests {
    @Test
    fun testEmpty() {
        val url = javaClass.classLoader.getResource("mapgroupproto/empty.xml")
        val path = Path.of(url.toURI())

        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> MapGroupProtoModel.getGroups(doc) }
            .orElseThrow()

        assertTrue { groups.isEmpty() }
    }

    @Test
    fun testSimple() {
        val url = javaClass.classLoader.getResource("mapgroupproto/simple.xml")
        val path = Path.of(url.toURI())
        val expected = mapOf(
            "Land_Shed_M1" to r(17, 4, 29, 12),
            "Land_Shed_M3" to r(30, 4, 44, 12),
        )

        val groups = DocumentUtils.tryParseDocument(path)
            .map { doc -> MapGroupProtoModel.getGroups(doc) }
            .orElseThrow()

        assertEquals(expected, groups)
    }
}
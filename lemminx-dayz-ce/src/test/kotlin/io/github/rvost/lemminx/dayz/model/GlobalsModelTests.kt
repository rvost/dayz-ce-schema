package io.github.rvost.lemminx.dayz.model

import io.github.rvost.lemminx.dayz.utils.DocumentUtils
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlobalsModelTests {
    @Test
    fun testMatchAnyFileName() {
        val url = javaClass.classLoader.getResource("globals/foo.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertTrue { GlobalsModel.match(doc) }
    }

    @Test
    fun testNotMatchIncompatibleScheme() {
        val url = javaClass.classLoader.getResource("globals/notGlobals.xml")
        val path = Path.of(url.toURI())

        val doc = DocumentUtils.tryParseDocument(path).orElseThrow();
        assertFalse { GlobalsModel.match(doc) }
    }
}
package net.soundvibe.jkob

import org.junit.Assert.*
import org.junit.Test

class StringSerializerTest {

    @Test
    fun `should support strings`() {
        val json = "Foobar".toJson()
        val jsonString = json.toString()
        assertEquals(""""Foobar"""", jsonString)
    }
}
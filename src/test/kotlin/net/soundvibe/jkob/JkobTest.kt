package net.soundvibe.jkob

import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author linas on 17.6.13.
 */
class JkobTest {

    @Test
    fun shouldBuildJson() {
        val json = json {
            "id" to "value"
            "name" to "foo"
            "items" ["value1", "value2", "value3"]
            for (i in 1..2) "item$i" to i
            "numbers" [1, 2, 3]
            "child" {
                "age" to 19
                "isValid" to false
                "isNull" to null
            }
        }

        assertEquals("value", json["id"]?.to<String>())
        assertEquals(listOf("value1", "value2", "value3"),
                json["items"]?.toList<String>())
        val map: Map<String, Any?> = mapOf(
                "age" to 19,
                "isValid" to false,
                "isNull" to null)
        assertEquals(map, json["child"]?.toMap<Any>())
        assertEquals(19, json["child"]?.get("age")?.to<Int>())
        assertEquals(listOf(1,2,3), json["numbers"]?.toList<Int>())
    }

    @Test
    fun shouldSerializeToString() {
        val jsonString = json {
            "foo" to "bar"
            "items" [1, 2, 3]
        }.toString()
        //language=JSON
        assertEquals("{\"foo\": \"bar\", \"items\": [1, 2, 3]}", jsonString)
    }
}
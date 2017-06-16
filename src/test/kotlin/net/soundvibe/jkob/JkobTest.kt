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
            "items" [
                 "value1",
                 "value2",
                 "value3"
            ]
            for (i in 1..2) "item$i" to i
            "numbers" [1, 2, 3]
            "child" {
                "age" to 19
                "isValid" to false
                "isNull" to null
            }
        }

        assertEquals(listOf("value1", "value2", "value3"),
                json["items"]?.ofArray<String>()
        )
        assertEquals("value", json["id"]?.of<String>())
        val map: Map<String, Any?> = mapOf("age" to 19,
                "isValid" to false,
                "isNull" to null)
        assertEquals(map,
                json["child"]?.ofObject<Any>())

        println(json.toString())
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
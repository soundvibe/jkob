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

        assertEquals("value", json["id"]?.to()!!)
        assertEquals(listOf("value1", "value2", "value3"),
                json["items"]?.toList<String>())
        val map: Map<String, Any?> = mapOf(
                "age" to 19,
                "isValid" to false,
                "isNull" to null)
        assertEquals(map, json["child"]?.toMap<Any>())
        assertEquals(19, json["child"]?.get("age")?.to()!!)
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

    @Test
    fun shouldDeclareObjectsInArrays() {
        val jsonString = json {
            "objects" [{
                "key" to "value"
            }, {
                "key2" to "value2"
            }]
        }.toString()

        //language=JSON
        assertEquals("{\"objects\": [{\"key\": \"value\"}, {\"key2\": \"value2\"}]}", jsonString)
    }

    @Test
    fun shouldEscapeValues() {
        val jsonString = json {
            "objects" [{
                "key" to """Company "Name""""
            }, {
                "key2" to "value2"
            }]
        }.toString()

        //language=JSON
        assertEquals("{\"objects\": [{\"key\": \"Company \\\"Name\\\"\"}, {\"key2\": \"value2\"}]}", jsonString)
    }

    @Test
    fun shouldBuildArraysAsPairs() {
        val json = json {
            "object" to {
                "key" to "value"
                "arrayInt" to arrayOf(1,2,3)
                "arrayString" to setOf("one", "two")
            }
        }
        //language=JSON
        assertEquals("{\"object\": {\"key\": \"value\", \"arrayInt\": [1, 2, 3], \"arrayString\": [\"one\", \"two\"]}}", json.toString())
    }

    @Test
    fun shouldGetAsMap() {
        val json = json {
            "object" to {
                "key" to "value"
                "number" to 10
            }
        }
        val map = json.toMap()

        assertEquals(JsString("value"), map["object"]?.get("key"))
        assertEquals(JsNumber(10), map["object"]?.get("number"))
    }
}
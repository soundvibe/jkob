package net.soundvibe.jkob

import net.soundvibe.jkob.data.*
import org.junit.Assert.*
import org.junit.Test

@Suppress("UNCHECKED_CAST")
class ObjectSerializerTest {

    data class Foo(val name: String, val bar: Bar)
    data class Bar(val prop: Int)

    private val json = "{\"bar\": {\"prop\": 19}, \"name\": \"Bob\"}"
    private val mapJson = "{\"foo\": {\"prop\": 20}, \"bar\": {\"bar\": {\"prop\": 11}, \"name\": \"one\"}}"

    @Test
    fun `should serialize data classes`() {
        val foo = Foo("Bob", Bar(19))
        val jsonString = foo.toJson().toString()
        assertEquals(json, jsonString)
    }

    @Test
    fun `should deserialize data class`() {
        val foo = json.fromJson<Foo>()
        assertEquals(Foo("Bob", Bar(19)), foo)
    }

    @Test
    fun `should serialize maps`() {
        val map = mapOf("foo" to Bar(20), "bar" to Foo("one", Bar(11)))
        val jsonString = map.toJson().toString()
        assertEquals(mapJson, jsonString)
    }

    @Test
    fun `should deserialize from maps`() {
        val actual = mapJson.fromJson<Map<String, *>>()

        assertFalse(actual == null)
        val foo = actual?.get("foo") as Map<String, *>
        assertEquals(20, foo["prop"] as Int)
        val bar = (actual["bar"] as Map<String, *>)["bar"] as Map<String, *>
        assertEquals(11, bar["prop"] as Int)
    }

    @Test
    fun `should deserialize to sealed class from data class`() {
        val laptop = Device.Laptop("Lenovo")
        val jsonString = laptop.toJson().toString()
        println(jsonString)

        val result = jsonString.fromJson<Device>()
        assertEquals(laptop, result)
    }

    @Test
    fun `should deserialize to sealed class from class`() {
        val mobile = Device.Mobile()
        mobile.imei = "123456"
        val jsonString = mobile.toJson().toString()
        println(jsonString)

        val result = jsonString.fromJson<Device>()
        assertEquals(mobile, result)
    }
}
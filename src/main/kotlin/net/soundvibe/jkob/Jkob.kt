package net.soundvibe.jkob

/**
 * @author Linas on 17.6.13.
 */

sealed class JsonValue {

    inline fun <reified T> of(): T? {
        return when (this) {
            is JsString -> value as T
            is JsBool -> boolean as T
            is JsNumber -> number as T
            is JsObject -> elements as T
            is JsArray -> elements as T
            is JsNull -> null
        }
    }

    inline fun <reified T> ofArray(): List<T?>? {
        return when (this) {
            is JsArray -> elements.map { it.of<T>() }
            is JsNull -> null
            else -> throw ClassCastException("Underlying json value is of different type: ${this.javaClass}")
        }
    }

    inline fun <reified T> ofObject(): Map<String, T?>? {
        return when (this) {
            is JsObject -> elements.mapValues { it.value.of<T>() }
            is JsNull -> null
            else -> throw ClassCastException("Underlying json value is of different type: ${this.javaClass}")
        }
    }

    override fun toString(): String {
        return when (this) {
            is JsString -> """"${this.value}""""
            is JsBool -> if (this.boolean) "true" else "false"
            is JsNumber -> this.number.toString()
            is JsObject -> this.toJsonString()
            is JsArray -> this.elements.asSequence()
                    .joinToString(prefix = "[", postfix = "]") { it.toString() }
            is JsNull -> "null"
        }
    }
}

class JsString(val value: String): JsonValue(), CharSequence by value
class JsBool(val boolean: Boolean): JsonValue()
class JsNumber(val number: Number): JsonValue()
class JsObject(val elements: LinkedHashMap<String, JsonValue>): JsonValue(), Map<String, JsonValue> by elements
class JsArray(val elements: List<JsonValue>): JsonValue(), List<Any> by elements
object JsNull: JsonValue()

class Jkob {

    private val entries = LinkedHashMap<String, JsonValue>()

    operator fun get(key: String) = entries[key]

    operator fun invoke(key: String) = this[key]

    infix fun String.to(value: JsonValue) {
        entries[this] = value
    }

    infix fun String.to(value: String) {
        entries[this] = JsString(value)
    }

    infix fun String.to(value: Number) {
        entries[this] = JsNumber(value)
    }

    infix fun String.to(value: Boolean?) {
        entries[this] = value?.let { JsBool(it) } ?: JsNull
    }

    infix fun String.to(value: Jkob.() -> Unit) {
        val json = json(value)
        entries[this] = JsObject(json.entries)
    }

    operator fun String.invoke(value: Jkob.() -> Unit) {
        to(value)
    }

    operator fun String.get(vararg values: JsonValue) {
        values.forEach { entries[this] = it }
    }

    operator fun String.get(vararg values: String) {
        entries[this] = JsArray(values.map { JsString(it) })
    }

    operator fun String.get(vararg values: Number) {
        entries[this] = JsArray(values.map { JsNumber(it) })
    }

    operator fun String.get(vararg values: Boolean) {
        entries[this] = JsArray(values.map { JsBool(it) })
    }

    override fun toString(): String {
        return entries.toJsonString()
    }
}

fun String.js() = JsString(this)
fun Number.js() = JsNumber(this)
fun Boolean.js() = JsBool(this)

fun Map<String, JsonValue>.toJsonString() = asSequence()
            .joinToString(prefix = "{", postfix = "}") {
                entry -> """"${entry.key}": ${entry.value}"""}


inline fun <T> json(body: Jkob.() -> T): Jkob {
    val json = Jkob()
    json.body()
    return json
}


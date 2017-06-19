package net.soundvibe.jkob

/**
 * @author Linas on 17.6.13.
 */

sealed class JsonValue {

    inline fun <reified T> to(): T? = when (this) {
            is JsString -> value as T
            is JsBool -> boolean as T
            is JsNumber -> number as T
            is JsObject -> elements as T
            is JsArray -> elements as T
            is JsNull -> null
    }

    inline fun <reified T> toList(): List<T?>? = when (this) {
            is JsArray -> elements.map { it.to<T>() }
            is JsNull -> null
            else -> throw ClassCastException("Underlying json value is to different type: ${this.javaClass}")
    }

    inline fun <reified T> toMap(): Map<String, T?>? = when (this) {
            is JsObject -> elements.mapValues { it.value.to<T>() }
            is JsNull -> null
            else -> throw ClassCastException("Underlying json value is to different type: ${this.javaClass}")
    }

    operator fun get(key: String): JsonValue? = when (this) {
            is JsObject -> this.elements[key]
            else -> null
    }

    operator fun get(ix: Int): JsonValue? {
        return when (this) {
            is JsArray -> this.elements[ix]
            else -> null
        }
    }
}

data class JsString(val value: String): JsonValue() {
    override fun toString() = """"$value""""
}
data class JsBool(val boolean: Boolean): JsonValue() {
    override fun toString() = if (boolean) "true" else "false"
}
data class JsNumber(val number: Number): JsonValue() {
    override fun toString() = number.toString()
}
data class JsObject(val elements: LinkedHashMap<String, JsonValue>): JsonValue() {
    override fun toString() = elements.toJsonString()
}
data class JsArray(val elements: List<JsonValue>): JsonValue() {
    override fun toString() = elements.asSequence()
                .joinToString(prefix = "[", postfix = "]") { it.toString() }
}
object JsNull: JsonValue() {
    override fun toString() = "null"
}

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
        entries[this] = toObject(value)
    }

    operator fun String.invoke(value: Jkob.() -> Unit) {
        to(value)
    }

    fun toObject(value: Jkob.() -> Unit): JsObject {
        val json = json(value)
        return JsObject(json.entries)
    }

    operator fun String.get(vararg objects: Jkob.() -> Unit) {
        entries[this] = JsArray(objects.map { toObject(it) })
    }

    operator fun String.get(vararg values: JsonValue) {
        entries[this] = JsArray(values.asList())
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

    override fun toString() = entries.toJsonString()
}

inline fun <T> json(body: Jkob.() -> T): Jkob {
    val json = Jkob()
    json.body()
    return json
}


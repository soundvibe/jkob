package net.soundvibe.jkob

import com.beust.klaxon.*
import java.io.StringReader
import kotlin.reflect.*

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class Sealed(val classPropertyName: String = "className")

inline fun <reified T> T?.toJson(): JsonValue {
    val serializer = DefaultSerializers.findSerializer<T>()
    return serializer.serialize(this)
}

val klaxon = Klaxon()

fun JsonBase.toJkob(): JsonValue =
    when (this) {
        is JsonObject -> JsObject(this.map.mapValues {
            val value = it.value
            when (value) {
                is JsonBase -> value.toJkob()
                else -> value.toJson()
        } })
        is JsonArray<*> -> JsArray(this.map {
            when (it) {
                is JsonBase -> it.toJkob()
                else -> it.toJson()
            }
        })
        else -> this.toJson()
    }

inline fun <reified T> fromJson(json: JsonValue): T? {
    val deSerializer = DefaultSerializers.findDeSerializer<T>()
    return deSerializer.deserialize(json, T::class)
}

inline fun <reified T> String.fromJson(): T? = fromJson(klaxon.parseJsonObject(StringReader(this)).toJkob())


interface Serializer<in T> {
    fun serialize(value: T?): JsonValue
}

interface DeSerializer<T: JsonValue, out R> {
    fun deserialize(value: T, returnClass: KClass<*>): R?
}

object DefaultSerializers {

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    inline fun <reified T> findSerializer(): Serializer<T> {
        return (serializers[T::class] ?: ObjectSerializer) as Serializer<T>
    }

    fun findSerializer(kClass: KClass<*>): Serializer<Any>? {
        @Suppress("UNCHECKED_CAST")
        return serializers[kClass] as Serializer<Any>?
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    inline fun <reified T> findDeSerializer(): DeSerializer<JsonValue, T> {
        return (deserializers[T::class] ?: ObjectSerializer) as DeSerializer<JsonValue, T>
    }

    fun findDeSerializer(kClass: KClass<*>): DeSerializer<JsonValue, Any>? {
        @Suppress("UNCHECKED_CAST")
        return deserializers[kClass] as DeSerializer<JsonValue,Any>?
    }

    val serializers: Map<KClass<*>, Serializer<*>> = mutableMapOf(
            String::class to StringSerializer,
            Char::class to CharSerializer,
            Number::class to NumberSerializer,
            Byte::class to ByteSerializer,
            Short::class to ShortSerializer,
            Int::class to IntSerializer,
            Long::class to LongSerializer,
            Double::class to DoubleSerializer,
            Float::class to FloatSerializer,
            Boolean::class to BooleanSerializer
    )

    val deserializers: Map<KClass<*>, DeSerializer<out JsonValue, *>> = mutableMapOf(
            String::class to StringSerializer,
            Char::class to CharSerializer,
            JsString::class to StringSerializer,
            JsNumber::class to NumberSerializer,
            Number::class to NumberSerializer,
            Double::class to DoubleSerializer,
            Float::class to FloatSerializer,
            Int::class to IntSerializer,
            Short::class to ShortSerializer,
            Byte::class to ByteSerializer,
            Long::class to LongSerializer,
            JsObject::class to ObjectSerializer,
            Any::class to ObjectSerializer,
            JsBool::class to BooleanSerializer,
            Boolean::class to BooleanSerializer,
            JsNull::class to NullDeserializer
    )
}


package net.soundvibe.jkob

import com.beust.klaxon.*
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl
import java.io.StringReader
import java.lang.reflect.*
import java.lang.reflect.Type
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

inline fun <reified T> parseJson(json: JsonValue): T? {
    val aClass = when (T::class) {
        List::class, Set::class, Collection::class -> resolveCollectionElementClass(object : TypeReference<T>() {}.type)
        Map::class -> resolveMapElementClass(object : TypeReference<T>() {}.type)
        else -> T::class
    }
    val deSerializer = DefaultSerializers.findDeSerializer<T>()
    return deSerializer.deserialize(json, aClass)
}

fun resolveCollectionElementClass(type: Type): KClass<*> {
    val rawType = ((type as ParameterizedType).actualTypeArguments.first() as WildcardTypeImpl).upperBounds.first()
    return (rawType as Class<*>).kotlin
}

fun resolveMapElementClass(type: Type): KClass<*> {
    val rawType = ((type as ParameterizedType).actualTypeArguments[1] as WildcardTypeImpl).upperBounds.first()
    return (rawType as Class<*>).kotlin
}

inline fun <reified T> String.parseJson(): T? = when (T::class) {
    List::class, Set::class, Collection::class -> {
        parseJson(klaxon.parseJsonArray(StringReader(this)).toJkob())
    }
    else -> parseJson(klaxon.parseJsonObject(StringReader(this)).toJkob())
}

fun deserializeToClass(kClass: KClass<*>, value: JsonValue): Any? =
        DefaultSerializers.findDeSerializer(kClass)?.deserialize(value, kClass) ?:
        ObjectSerializer.deserialize(value, kClass)


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
            Number::class to NumberSerializer,
            Double::class to DoubleSerializer,
            Float::class to FloatSerializer,
            Int::class to IntSerializer,
            Short::class to ShortSerializer,
            Byte::class to ByteSerializer,
            Long::class to LongSerializer,
            Any::class to ObjectSerializer,
            Boolean::class to BooleanSerializer,
            Collection::class to CollectionDeSerializer,
            List::class to ListDeSerializer,
            Set::class to SetDeSerializer,
            Map::class to MapDeSerializer
    )
}

abstract class TypeReference<T> {

    val type: java.lang.reflect.Type

    @Volatile
    private var constructor: Constructor<*>? = null

    init {
        val superclass = javaClass.genericSuperclass
        if (superclass is Class<*>) {
            throw RuntimeException("Missing type parameter.")
        }
        type = (superclass as ParameterizedType).actualTypeArguments[0]
    }
}


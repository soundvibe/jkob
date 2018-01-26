package net.soundvibe.jkob

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

object ObjectSerializer: Serializer<Any>, DeSerializer<JsonValue,Any> {

    override fun serialize(value: Any?): JsonValue = when (value) {
        null -> JsNull
        else -> serializeValue(value)
    }

    private fun serializeValue(value: Any): JsonValue {
        val kClass = value::class
        val serializer = DefaultSerializers.findSerializer(kClass)
        if (serializer != null) {
            return serializer.serialize(value)
        }
        return when (value) {
            is Map<*,*> -> JsObject(value
                    .mapKeys { it.key.toString() }
                    .mapValues { it.value.toJson() })
            is Collection<*> -> JsArray(value.map { it.toJson() })
            is Enum<*> -> StringSerializer.serialize(value.name)
            else -> {
                JsObject(kClass.memberProperties
                        .associateBy( { it.name }, {
                            @Suppress("UNCHECKED_CAST")
                            (it as KProperty1<Any, Any>).get(value).toJson()
                        }))
            }
        }
    }

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Any? = when (value) {
        is JsObject -> deserializeJsObject(value, returnClass)
        is JsArray -> deserializeJsArray(value, returnClass)
        is JsString -> when {
            returnClass.isSubclassOf(Enum::class) -> returnClass.java.enumConstants.first {
                (it as Enum<*>).name == StringSerializer.deserialize(value, returnClass) }
            else -> StringSerializer.deserialize(value, returnClass)
        }
        is JsNumber -> NumberSerializer.deserialize(value, returnClass)
        is JsBool -> BooleanSerializer.deserialize(value, returnClass)
        JsNull -> null
    }

    private fun deserializeValue(value: JsonValue): Any? = when (value) {
        is JsString -> value.value
        is JsBool -> value.boolean
        is JsNumber -> value.number
        is JsObject -> value.elements.mapValues { deserializeValue(it.value) }
        is JsArray -> value.elements.map { deserializeValue(it) }
        JsNull -> null
    }

    private fun deserializeJsArray(value: JsArray, returnClass: KClass<*>): Any? = when {
        returnClass.isSubclassOf(Collection::class) -> value.elements.map { deserializeValue(it) }
        else -> throw IllegalStateException("Cannot map from JsArray to $returnClass")
    }

    private fun deserializeJsObject(value: JsObject, returnClass: KClass<*>): Any? = when {
        returnClass.isData -> {
            val primaryConstructor = returnClass.primaryConstructor!!
            val args = primaryConstructor.parameters
                    .associate { it to resolveValue(it, value) }

            primaryConstructor.callBy(args)
        }
        returnClass.isSealed -> deserializeSealed(value, returnClass)
        returnClass.isCompanion -> returnClass.companionObjectInstance
        returnClass.isSubclassOf(Map::class) -> value.elements.mapValues { deserializeValue(it.value) }
        else -> deserializeClass(value, returnClass)
    }

    private fun deserializeClass(value: JsObject, returnClass: KClass<*>): Any? {
        val primaryConstructor = returnClass.primaryConstructor!!
        val args = primaryConstructor.parameters
                .associate { it to resolveValue(it, value) }
        val resultClass = primaryConstructor.callBy(args)

        returnClass.memberProperties.asSequence()
                .filterIsInstance(KMutableProperty::class.java)
                .forEach {
                    value[it.name]?.run {
                        it.setter.call(resultClass, deserializeValue(this)) }
                }
        return resultClass
    }

    private fun deserializeSealed(value: JsonValue, returnClass: KClass<*>): Any? {
        val classPropertyName = returnClass.findAnnotation<Sealed>()?.classPropertyName ?: "className"
        val className = (value[classPropertyName] as JsString).value

        val concreteClass = returnClass.nestedClasses
                .first { className == it.simpleName }
        return deserialize(value, concreteClass)
    }
}

private fun resolveValue(parameter: KParameter, value: JsonValue): Any? {
    val deSerializer = DefaultSerializers.findDeSerializer(parameter.type.jvmErasure) ?: ObjectSerializer
    return deSerializer.deserialize(
            value[parameter.name ?: ""] ?: throw NoSuchFieldException("${parameter.name} was not found in $value"),
            parameter.type.jvmErasure)
}

object CharSerializer: Serializer<Char>, DeSerializer<JsonValue, Char> {

    override fun serialize(value: Char?): JsonValue = when (value) {
        null -> JsNull
        else -> JsString(value.toString())
    }

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Char? = when (value) {
        is JsString -> value.value[0]
        is JsBool -> value.boolean.toString()[0]
        is JsNumber -> value.number.toString()[0]
        JsNull -> null
        else -> value.toString()[0]
    }
}

object StringSerializer: Serializer<String>, DeSerializer<JsonValue, String> {

    override fun serialize(value: String?): JsonValue = when (value) {
        null -> JsNull
        else -> JsString(value)
    }

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): String? = when (value) {
        is JsString -> value.value
        is JsBool -> value.boolean.toString()
        is JsNumber -> value.number.toString()
        JsNull -> null
        else -> value.toString()
    }
}

object FloatSerializer: Serializer<Float>, DeSerializer<JsonValue, Float> {
    override fun serialize(value: Float?): JsonValue = NumberSerializer.serialize(value)

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Float? = when (value) {
        is JsNumber -> value.number.toFloat()
        JsNull -> null
        is JsString -> value.value.toFloat()
        is JsBool -> if (value.boolean) 1f else 0f
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Float")
    }
}

object DoubleSerializer: Serializer<Double>, DeSerializer<JsonValue, Double> {
    override fun serialize(value: Double?): JsonValue = NumberSerializer.serialize(value)

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Double? = when (value) {
        is JsNumber -> value.number.toDouble()
        JsNull -> null
        is JsString -> value.value.toDouble()
        is JsBool -> if (value.boolean) 1.0 else 0.0
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Double")
    }
}

object NumberSerializer: Serializer<Number>, DeSerializer<JsonValue, Number> {
    override fun serialize(value: Number?): JsonValue = when (value) {
        null -> JsNull
        else -> JsNumber(value)
    }

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Number? = when (value) {
        is JsNumber -> value.number
        JsNull -> null
        is JsString -> value.value.toBigDecimal()
        is JsBool -> if (value.boolean) 1 else 0
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Number")
    }
}

object ByteSerializer: Serializer<Int>, DeSerializer<JsonValue, Byte> {
    override fun serialize(value: Int?): JsonValue = NumberSerializer.serialize(value)

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Byte? = when (value) {
        is JsNumber -> value.number.toByte()
        JsNull -> null
        is JsString -> value.value.toByte()
        is JsBool -> if (value.boolean) 1 else 0
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Byte")
    }
}

object ShortSerializer: Serializer<Int>, DeSerializer<JsonValue, Short> {
    override fun serialize(value: Int?): JsonValue = NumberSerializer.serialize(value)

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Short? = when (value) {
        is JsNumber -> value.number.toShort()
        JsNull -> null
        is JsString -> value.value.toShort()
        is JsBool -> if (value.boolean) 1 else 0
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Short")
    }
}

object IntSerializer: Serializer<Int>, DeSerializer<JsonValue, Int> {
    override fun serialize(value: Int?): JsonValue = NumberSerializer.serialize(value)

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Int? = when (value) {
        is JsNumber -> value.number.toInt()
        JsNull -> null
        is JsString -> value.value.toInt()
        is JsBool -> if (value.boolean) 1 else 0
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Int")
    }
}

object LongSerializer: Serializer<Long>, DeSerializer<JsonValue, Long> {
    override fun serialize(value: Long?): JsonValue = NumberSerializer.serialize(value)

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Long? = when (value) {
        is JsNumber -> value.number.toLong()
        JsNull -> null
        is JsString -> value.value.toLong()
        is JsBool -> if (value.boolean) 1L else 0L
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Long")
    }
}

object NullDeserializer: DeSerializer<JsNull,Nothing> {
    override fun deserialize(value: JsNull, returnClass: KClass<*>): Nothing? = null
}

object BooleanSerializer: Serializer<Boolean>, DeSerializer<JsonValue,Boolean> {
    override fun serialize(value: Boolean?): JsonValue = when (value) {
        null -> JsNull
        else -> JsBool(value)
    }

    override fun deserialize(value: JsonValue, returnClass: KClass<*>): Boolean? = when (value) {
        is JsBool -> value.boolean
        JsNull -> null
        is JsNumber -> value.number.toInt() == 1
        is JsString -> value.value.toBoolean()
        else -> throw UnsupportedOperationException("Cannot deserialize from $value to Boolean")
    }
}
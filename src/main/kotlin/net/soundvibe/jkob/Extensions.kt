package net.soundvibe.jkob

/**
 * @author linas on 17.6.16.
 */

val String?.js get() = if (this != null) JsString(this) else JsNull
val Number?.js get() = if (this != null) JsNumber(this) else JsNull
val Boolean?.js get() = if (this != null) JsBool(this) else JsNull

fun Map<String, JsonValue>.toJsonString() = asSequence()
        .joinToString(prefix = "{", postfix = "}") {
            entry -> """"${entry.key}": ${entry.value}"""}
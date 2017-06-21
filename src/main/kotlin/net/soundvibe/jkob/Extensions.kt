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

fun String.toJsQuote(): String {
    val product = StringBuilder()
    product.append("\"")
    val chars = this.toCharArray()
    chars.forEach {
        when (it) {
            '\b' -> product.append("\\b")
            '\t' -> product.append("\\t")
            '\n' -> product.append("\\n")
            '\r' -> product.append("\\r")
            '"' -> product.append("\\\"")
            '\\' -> product.append("\\\\")
            else -> if (it.toInt() < 32) {
                product.append(unicodeEscape(it))
            } else {
                product.append(it)
            }
        }
    }
    product.append("\"")
    return product.toString()
}

fun unicodeEscape(ch: Char): String {
    val sb = StringBuilder()
    sb.append("\\u")
    val hex = Integer.toHexString(ch.toInt())

    for (i in hex.length..3) {
        sb.append('0')
    }

    sb.append(hex)
    return sb.toString()
}
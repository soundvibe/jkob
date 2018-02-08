package net.soundvibe.jkob.data

import net.soundvibe.jkob.Sealed

sealed class Result
open class Success<out T>(val value: T): Result()
object Failure: Result()
class MoreSuccess<T>(val value2: T): Success<T>(value2)

@Sealed("name")
sealed class Device {
    val name: String
        get() = this::class.simpleName ?: "Device"
}

data class Laptop(val manufacturer: String): Device()
data class Workstation(val manufacturer: String): Device()
class Mobile(var manufacturer: String = "Apple"): Device() {
    var imei: String = "default"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Mobile) return false

        if (manufacturer != other.manufacturer) return false
        if (imei != other.imei) return false
        return true
    }

    override fun hashCode(): Int {
        var result = manufacturer.hashCode()
        result = 31 * result + imei.hashCode()
        return result
    }
}

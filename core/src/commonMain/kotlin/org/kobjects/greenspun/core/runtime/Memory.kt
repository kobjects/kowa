package org.kobjects.greenspun.core.runtime

class Memory(
    val initial: Int,
    val max: Int? = null) {

    var buffer = ByteArray(initial * 65536)

    fun grow(by: Int) {
        buffer = buffer.copyOf(buffer.size + by * 65536)
    }
}
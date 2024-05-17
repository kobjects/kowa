package org.kobjects.kowa.runtime

class Memory(
    val initial: Int,
    val max: Int? = null) {

    var bytes = ByteArray(initial * 65536)

    fun growBy(by: Int) {
        bytes = bytes.copyOf(bytes.size + by * 65536)
    }
}
package org.kobjects.greenspun.core.wasm

import kotlin.math.max

class WasmWriter {
    var data: ByteArray = ByteArray(0)
    var size = 0

    fun ensureCapacity(capacity: Int) {
        if (data.size < capacity) {
            data = data.copyOf(max(capacity, data.size * 3 / 2))
        }
    }

    fun write(byte: Byte) {
        ensureCapacity(size + 1)
        data[size++] = byte
    }

}
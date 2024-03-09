package org.kobjects.greenspun.core.wasm

import org.kobjects.greenspun.core.module.Module
import kotlin.math.max

class WasmWriter(val module: Module) {
    var data: ByteArray = ByteArray(0)
    var size = 0

    fun ensureCapacity(capacity: Int) {
        if (data.size < capacity) {
            data = data.copyOf(max(capacity, data.size * 3 / 2))
        }
    }

    fun write(opcode: WasmOpcode) {
        ensureCapacity(size + 1)
        data[size++] = opcode.code.toByte()
    }

    fun writeVarUInt32(value: Int) {
        throw UnsupportedOperationException()
    }

    fun toByteArray() = data.copyOf(size)
}
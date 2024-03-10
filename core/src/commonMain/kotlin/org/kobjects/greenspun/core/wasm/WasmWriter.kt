package org.kobjects.greenspun.core.wasm

import org.kobjects.greenspun.core.module.Module
import kotlin.math.max

class WasmWriter(val module: Module) {
    var data: ByteArray = ByteArray(0)
    var size = 0

    private fun ensureCapacity(capacity: Int) {
        if (data.size < capacity) {
            data = data.copyOf(max(capacity, data.size * 3 / 2))
        }
    }

    fun write(value: Byte) {
        ensureCapacity(size + 1)
        data[size++] = value
    }

    fun write(opcode: WasmOpcode) {
        write(opcode.code.toByte())
    }

    fun writeVarInt32(value: Int) {
        throw UnsupportedOperationException()
    }

    fun writeVarUInt32(value: UInt) {
        throw UnsupportedOperationException()
    }

    fun writeUInt32(value: UInt) {
        throw UnsupportedOperationException()
    }

    fun writeUint64(value: ULong) {
        throw UnsupportedOperationException()
    }

    fun writeVarInt64(value: Long) {
        throw UnsupportedOperationException()
    }

    fun toByteArray() = data.copyOf(size)
}
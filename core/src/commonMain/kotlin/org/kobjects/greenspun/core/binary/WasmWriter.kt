package org.kobjects.greenspun.core.binary

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

    fun write(type: WasmType) {
        write(type.code)
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

    fun writeUInt32(value: Int) {
        writeUInt32(value.toUInt())
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

    fun write(bytes: ByteArray) {
        for (byte in bytes) {
            write(byte)
        }
    }

    fun writeName(module: String) {
        throw UnsupportedOperationException()
    }
}
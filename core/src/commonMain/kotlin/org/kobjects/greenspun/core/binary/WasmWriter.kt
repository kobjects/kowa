package org.kobjects.greenspun.core.binary

import kotlin.math.max

open class WasmWriter {
    var data: ByteArray = ByteArray(0)
    var size = 0

    private fun ensureCapacity(capacity: Int) {
        if (data.size < capacity) {
            data = data.copyOf(max(capacity, data.size * 3 / 2))
        }
    }

    fun writeByte(value: Byte) {
        ensureCapacity(size + 1)
        data[size++] = value
    }

    fun writeByte(value: Int) = writeByte(value.toByte())

    fun writeInt(value: Int) {
        ensureCapacity(size + 4)
        data.storeI32(size, value)
        size += 4
    }

    fun writeLong(value: Long) {
        ensureCapacity(size + 8)
        data.storeI64(size, value)
        size += 8
    }
    fun writeULong(value: ULong) {
        writeLong(value.toLong())
    }

    fun writeUInt(value: UInt) {
        ensureCapacity(size + 4)
        data[size++] = (value and 255u).toByte()
        data[size++] = ((value shr 8) and 255u).toByte()
        data[size++] = ((value shr 16) and 255u).toByte()
        data[size++] = ((value shr 24) and 255u).toByte()
    }

    fun writeF32(value: Float) = writeInt(value.toBits())

    fun writeF64(value: Double) = writeLong(value.toBits())

    fun writeAny(value: Any) {
        when (value) {
            is Byte -> writeByte(value)
            is Int -> writeInt(value)
            is UInt -> writeUInt(value)
            is Long -> writeLong(value)
            is ULong -> writeULong(value)
            is String -> writeUtf8(value)
            else -> throw IllegalArgumentException("Unsupported type for $value")
        }
    }


    fun write(type: WasmType) {
        writeByte(type.code)
    }

    fun write(opcode: WasmOpcode) {
        writeByte(opcode.code.toByte())
    }

    fun writeU32(value: UInt) = writeU32(value.toInt())


    fun writeU32(value: Int) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder ushr 7
            if (remainder == 0) {
                writeByte(b.toByte())
                break
            }
            writeByte((b or 0x80).toByte())
        }
    }

    fun writeI32(value: Int) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder shr 7
            if ((remainder == 0 && (b and 0x40) == 0) ||
                (remainder == -1 && (b and 0x40) != 0)) {
                writeByte(b)
                break
            }
            writeByte(b or 0x80)
        }
    }

    fun writeU64(value: UInt) = writeU64(value.toLong())

    fun writeU64(value: Long) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder ushr 7
            if (remainder == 0L) {
                writeByte(b.toByte())
                break
            }
            writeByte((b or 0x80).toByte())
        }
    }


    fun writeI64(value: Long) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder shr 7
            if ((remainder == 0L && (b and 0x40) == 0L) ||
                (remainder == -1L && (b and 0x40) != 0L)) {
                writeByte(b.toByte())
                break
            }
            writeByte((b or 0x80).toByte())
        }
    }

    fun toByteArray() = data.copyOf(size)

    fun writeBytes(bytes: ByteArray) {
        for (byte in bytes) {
            writeByte(byte)
        }
    }

    fun writeUtf8(value: String) =
        writeBytes(value.encodeToByteArray())


    fun writeName(value: String) {
        val encoded = value.encodeToByteArray()
        writeU32(encoded.size)
        writeBytes(encoded)
    }
}
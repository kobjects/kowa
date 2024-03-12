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

    fun write(value: Byte) {
        ensureCapacity(size + 1)
        data[size++] = value
    }

    fun write(value: Int) {
        ensureCapacity(size + 4)
        data.storeI32(size, value)
        size += 4
    }

    fun write(value: Long) {
        ensureCapacity(size + 8)
        data.storeI64(size, value)
        size += 8
    }
    fun write(value: ULong) {
        write(value.toLong())
    }

    fun write(value: UInt) {
        ensureCapacity(size + 4)
        data[size++] = (value and 255u).toByte()
        data[size++] = ((value shr 8) and 255u).toByte()
        data[size++] = ((value shr 16) and 255u).toByte()
        data[size++] = ((value shr 24) and 255u).toByte()
    }

    fun write(value: Float) = write(value.toBits())

    fun write(value: Double) = write(value.toBits())

    fun writeAny(value: Any) {
        when (value) {
            is Byte -> write(value)
            is Int -> write(value)
            is UInt -> write(value)
            is Long -> write(value)
            is ULong -> write(value)
            is String -> write(value)
            else -> throw IllegalArgumentException("Unsupported type for $value")
        }
    }

    fun write(type: WasmType) {
        write(type.code)
    }

    fun write(opcode: WasmOpcode) {
        write(opcode.code.toByte())
    }

    fun writeU32(value: UInt) = writeU32(value.toInt())


    fun writeU32(value: Int) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder ushr 7
            if (remainder == 0) {
                write(b.toByte())
                break
            }
            write((b or 0x80).toByte())
        }
    }

    fun writeI32(value: Int) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder shr 7
            if ((remainder == 0 && (b and 0x40) == 0) ||
                (remainder == -1 && (b and 0x40) != 0)) {
                write(b.toByte())
                break
            }
            write((b or 0x80).toByte())
        }
    }

    fun writeU64(value: UInt) = writeU64(value.toLong())

    fun writeU64(value: Long) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder ushr 7
            if (remainder == 0L) {
                write(b.toByte())
                break
            }
            write((b or 0x80).toByte())
        }
    }


    fun writeI64(value: Long) {
        var remainder = value
        while (true) {
            val b = remainder and 0x7f
            remainder = remainder shr 7
            if ((remainder == 0L && (b and 0x40) == 0L) ||
                (remainder == -1L && (b and 0x40) != 0L)) {
                write(b.toByte())
                break
            }
            write((b or 0x80).toByte())
        }
    }

    fun toByteArray() = data.copyOf(size)

    fun write(bytes: ByteArray) {
        for (byte in bytes) {
            write(byte)
        }
    }

    fun write(value: String) =
        write(value.encodeToByteArray())

}
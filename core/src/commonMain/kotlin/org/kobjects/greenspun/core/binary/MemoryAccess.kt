package org.kobjects.greenspun.core.binary


fun ByteArray.loadI16(offset: Int): Short =
    (this[offset].toUByte().toInt() or
            (this[offset + 1].toUByte().toInt() shl 8)).toShort()

fun ByteArray.loadU16(offset: Int) = loadI16(offset).toUShort()

fun ByteArray.loadI32(offset: Int): Int =
    (this[offset].toUByte().toInt() or
    (this[offset + 1].toUByte().toInt() shl 8) or
    (this[offset + 2].toUByte().toInt() shl 16) or
    (this[offset + 3].toUByte().toInt() shl 24))

fun ByteArray.loadU32(offset: Int) = loadI32(offset).toUInt()


fun ByteArray.loadI64(offset: Int): Long =
    (this[offset].toUByte().toLong() or
    (this[offset + 1].toUByte().toLong() shl 8) or
    (this[offset + 2].toUByte().toLong() shl 16) or
    (this[offset + 3].toUByte().toLong() shl 24) or
    (this[offset + 4].toUByte().toLong() shl 32) or
    (this[offset + 5].toUByte().toLong() shl 40) or
    (this[offset + 6].toUByte().toLong() shl 48) or
    (this[offset + 7].toUByte().toLong() shl 56))

fun ByteArray.loadU64(offset: Int) = loadI64(offset).toULong()

fun ByteArray.loadF32(offset: Int) = Float.fromBits(loadI32(offset))

fun ByteArray.loadF64(offset: Int) = Double.fromBits(loadI64(offset))


fun ByteArray.storeI16(offset: Int, value: Short) =
    storeI16(offset, value.toInt())

fun ByteArray.storeI16(offset: Int, value: Int) {
    this[offset] = (value and 255).toByte()
    this[offset + 1] = ((value shr 8) and 255).toByte()
}

fun ByteArray.storeI32(offset: Int, value: Int) {
    this[offset] = (value and 255).toByte()
    this[offset + 1] = ((value shr 8) and 255).toByte()
    this[offset + 2] = ((value shr 16) and 255).toByte()
    this[offset + 3] = ((value shr 24) and 255).toByte()
}

fun ByteArray.storeI64(offset: Int, value: Long) {
    this[offset] = (value and 255).toByte()
    this[offset + 1] = ((value shr 8) and 255).toByte()
    this[offset + 2] = ((value shr 16) and 255).toByte()
    this[offset + 3] = ((value shr 24) and 255).toByte()
    this[offset + 4] = ((value shr 32) and 255).toByte()
    this[offset + 5] = ((value shr 40) and 255).toByte()
    this[offset + 6] = ((value shr 48) and 255).toByte()
    this[offset + 7] = ((value shr 56) and 255).toByte()
}

fun ByteArray.storeF64(offset: Int, value: Double) = storeI64(offset, value.toBits())

fun ByteArray.storeF32(offset: Int, value: Float) = storeI32(offset, value.toBits())


const val HEX_DIGITS = "0123456789ABCDEF"
fun ByteArray.hexDump(): String {
    val sb = StringBuilder()
    for (i in this.indices) {
        sb.append(HEX_DIGITS[this[i] / 16])
        sb.append(HEX_DIGITS[this[i] % 16])
        sb.append(if (i % 16 == 15) '\n' else ' ')
    }
    return sb.toString()
}
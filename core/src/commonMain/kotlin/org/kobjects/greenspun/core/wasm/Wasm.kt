package org.kobjects.greenspun.core.wasm

object Wasm {
    const val UNREACHABLE: Byte = 0
    const val NOP: Byte = 1
    const val BLOCK: Byte = 2
    const val LOOP: Byte = 3
    const val IF: Byte = 4
    const val ELSE: Byte = 5
    const val END: Byte = 0x0B
    const val BR: Byte = 0x0C
    const val BR_IF: Byte = 0x0D
    const val BR_TABLE: Byte = 0x0E
    const val RETURN: Byte = 0x0F

    const val CALL: Byte = 0x10
    const val CALL_INDIRECT: Byte = 0x11

    const val BLOCKTYPE_NONE: Byte = 0x40
}
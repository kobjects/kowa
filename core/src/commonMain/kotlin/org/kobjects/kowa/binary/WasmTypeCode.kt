package org.kobjects.kowa.binary

enum class WasmTypeCode(val code: Byte) {
    VOID(0x40),
    I32(0x7f),
    I64(0x7e),
    F32(0x7d),
    F64(0x7f),

    V128(0x7b),

    FUNC_REF(0x70),
    EXTERN_REF(0x6f),

    FUNC(0x60)
}
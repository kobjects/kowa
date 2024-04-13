package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.binary.WasmTypeCode
import org.kobjects.greenspun.core.binary.WasmWriter

object FuncRef : org.kobjects.greenspun.core.type.WasmType {
    override fun toWasm(writer: WasmWriter) {
        writer.writeTypeCode(WasmTypeCode.FUNC_REF)
    }
}
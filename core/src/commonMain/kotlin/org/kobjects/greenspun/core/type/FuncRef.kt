package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.binary.WasmTypeCode
import org.kobjects.greenspun.binary.WasmWriter

object FuncRef : org.kobjects.greenspun.core.type.Type {
    override fun toWasm(writer: WasmWriter) {
        writer.writeTypeCode(WasmTypeCode.FUNC_REF)
    }
}
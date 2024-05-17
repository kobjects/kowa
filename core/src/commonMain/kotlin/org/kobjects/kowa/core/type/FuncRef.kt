package org.kobjects.kowa.core.type

import org.kobjects.kowa.binary.WasmTypeCode
import org.kobjects.kowa.binary.WasmWriter

object FuncRef : org.kobjects.kowa.core.type.Type {
    override fun toWasm(writer: WasmWriter) {
        writer.writeTypeCode(WasmTypeCode.FUNC_REF)
    }
}
package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter

class FuncRef : Type {
    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmType.FUNC_REF)
    }
}
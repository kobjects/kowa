package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.type.WasmType

interface GlobalInterface : Exportable {
    val index: Int
    val mutable: Boolean
    val type: WasmType


    override fun writeExportDescription(writer: WasmWriter) {
        writer.writeByte(3)
        writer.writeU32(index)
    }

    override fun writeExportDescription(writer: CodeWriter) {
        if (mutable) {
            writer.write("var$index")
        } else {
            writer.write("const$index")
        }
    }
}
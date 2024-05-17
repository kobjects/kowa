package org.kobjects.kowa.core.global

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.module.Exportable
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.type.Type

interface GlobalInterface : Exportable {
    val index: Int
    val mutable: Boolean
    val type: Type


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
package org.kobjects.kowa.core.func

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.module.Exportable
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.type.FuncType
import org.kobjects.kowa.runtime.Stack

interface FuncInterface : Exportable {
    val index: Int
    val type: FuncType

    fun call(context: Stack)

    override fun writeExportDescription(writer: WasmWriter) {
        writer.writeByte(0)
        writer.writeU32(index)
    }

    override fun writeExportDescription(writer: CodeWriter) {
        writer.write("func$index")
    }
}
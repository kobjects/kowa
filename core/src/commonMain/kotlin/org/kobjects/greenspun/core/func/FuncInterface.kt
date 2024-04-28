package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.runtime.Stack

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
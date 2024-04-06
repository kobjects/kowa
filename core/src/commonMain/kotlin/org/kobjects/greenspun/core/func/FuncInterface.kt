package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.expression.CodeWriter
import org.kobjects.greenspun.core.type.FuncType

interface FuncInterface : Exportable {
    val index: Int
    val type: FuncType

    fun call(context: LocalRuntimeContext, vararg params: Any): Any

    override fun writeExportDescription(writer: WasmWriter) {
        writer.writeByte(0)
        writer.writeU32(index)
    }

    override fun writeExportDescription(writer: CodeWriter) {
        writer.write("func$index")
    }
}
package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Idx
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType

interface FuncInterface : Exportable, Idx {
    override val index: Int
    val type: FuncType

    fun call(context: LocalRuntimeContext, vararg params: Node): Any

    operator fun invoke(vararg node: Any) =
        Call(this, *node.map { Node.of(it) }.toTypedArray())

    override fun writeExportDescription(writer: WasmWriter) {
        writer.writeByte(0)
        writer.writeU32(index)
    }

    override fun writeExportDescription(writer: CodeWriter) {
        writer.write("func$index")
    }
}
package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.Type

class IndirectCallNode(
    val table: Int,
    val index: Node,
    val funcType: FuncType,
    vararg val parameter: Node) : Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        val i = index.evalI32(context)
        // TODO: context.instance.tables...
        throw UnsupportedOperationException()
    }

    override fun children() =
        listOf(index) + parameter.toList()

    override fun reconstruct(newChildren: List<Node>) =
        IndirectCallNode(table, newChildren[0], funcType, *newChildren.subList(1, newChildren.size).toTypedArray())


    override fun toString(writer: CodeWriter) {
        writer.write("CallIndirect(", table, ", ", index, ", ", funcType.returnType)
        for (p in parameter) {
            writer.write(", ")
            writer.write(p)
        }
        writer.write(")")
    }

    override fun toWasm(writer: ModuleWriter) {
        writer.write(WasmOpcode.CALL_INDIRECT)
        writer.writeU32(funcType.index)
        writer.writeU32(0)
    }

    override val returnType: Type
        get() = funcType.returnType


}
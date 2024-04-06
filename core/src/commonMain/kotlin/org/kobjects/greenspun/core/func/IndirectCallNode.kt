package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expression.CodeWriter
import org.kobjects.greenspun.core.expression.Node
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.Type

class IndirectCallNode(
    val table: Int,
    val index: Node,
    val funcType: FuncType,
    vararg val parameter: Node
) : Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        val i = index.evalI32(context)
        val table = context.instance.tables[table]
        val f = table.elements[i] as FuncInterface
        return f.call(context, *parameter)
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

    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmOpcode.CALL_INDIRECT)
        writer.writeU32(funcType.index)
        writer.writeU32(0)
    }

    override val returnType: Type
        get() = funcType.returnType


}
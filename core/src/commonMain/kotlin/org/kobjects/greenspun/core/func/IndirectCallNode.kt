package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.Type

class IndirectCallNode(
    val table: Int,
    val index: Expr,
    val funcType: FuncType,
    vararg val parameter: Expr
) : Expr() {

    override fun children() =
        listOf(index) + parameter.toList()


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
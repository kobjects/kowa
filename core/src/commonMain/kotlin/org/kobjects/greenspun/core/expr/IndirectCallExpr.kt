package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.WasmType

class IndirectCallExpr(
    val table: Int,
    index: Expr,
    val funcType: FuncType,
    vararg parameter: Expr
) : Expr(*parameter, index) {

    override fun toString(writer: CodeWriter) {
        writer.write("CallIndirect(", table, ", ", children.last(), ", ", funcType.returnType)
        for (p in children.subList(0, children.size - 1)) {
            writer.write(", ")
            writer.write(p)
        }
        writer.write(")")
    }

    override fun toWasm(writer: WasmWriter) {
        super.toWasm(writer)
        writer.write(WasmOpcode.CALL_INDIRECT)
        writer.writeU32(funcType.index)
        writer.writeU32(table)
    }

    override val returnType: List<WasmType>
        get() = funcType.returnType


}
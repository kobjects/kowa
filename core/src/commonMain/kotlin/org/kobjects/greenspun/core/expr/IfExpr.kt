package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.Bool

class IfExpr(
    condition: Expr,
    then: Expr,
    otherwise: Expr
) : Expr(condition, then, otherwise) {

    init {
        require(condition.returnType == listOf(Bool)) {
            "Condition must be boolean."
        }
        require( otherwise.returnType == then.returnType) {
            "'then' (${then.returnType}) and 'else' (${otherwise.returnType}) types must match."
        }
    }

    override fun toString(writer: CodeWriter) {
        writer.write("If(", children[0])
        writer.write(", ", children[1], ", ", children[2], ")")
    }

    override fun toWasm(writer: WasmWriter) {
        children[0].toWasm(writer)
        writer.write(WasmOpcode.IF)
        returnType[0].toWasm(writer)
        children[1].toWasm(writer)
        writer.write(WasmOpcode.ELSE)
        children[2].toWasm(writer)
        writer.write(WasmOpcode.END)
    }

    override val returnType: List<Type>
        get() = children[1].returnType

}
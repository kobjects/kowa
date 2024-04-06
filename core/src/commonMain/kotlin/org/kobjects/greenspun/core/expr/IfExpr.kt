package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter

class IfExpr(
    val condition: Expr,
    val then: Expr,
    val otherwise: Expr
) : Expr() {

    init {

            require( otherwise.returnType == then.returnType) {
                "'then' (${then.returnType}) and 'else' (${otherwise.returnType}) types must match."
            }

    }

    override fun eval(context: LocalRuntimeContext): Any {
        throw UnsupportedOperationException()
    }

    override fun children() = listOf(condition, then, otherwise)

    override fun reconstruct(newChildren: List<Expr>) =
        IfExpr(newChildren[0], newChildren[1], newChildren[2])

    override fun toString(writer: CodeWriter) {
        writer.write("If(", condition)
        writer.write(", ", then, ", ", otherwise, ")")
    }

    override fun toWasm(writer: WasmWriter) {
        condition.toWasm(writer)
        writer.write(WasmOpcode.IF)
        returnType.toWasm(writer)
        then.toWasm(writer)
        writer.write(WasmOpcode.ELSE)
        otherwise.toWasm(writer)
        writer.write(WasmOpcode.END)
    }

    override val returnType: Type
        get() = then.returnType

}
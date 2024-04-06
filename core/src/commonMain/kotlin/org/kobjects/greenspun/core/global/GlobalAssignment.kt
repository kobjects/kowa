package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.binary.WasmWriter

open class GlobalAssignment(
    val global: GlobalInterface,
    val expression: Expr
) : Expr() {

    init {
        require(global.mutable) {
            "Can't set const"
        }
        require(global.type == expression.returnType) {
            "Global variable type ${global.type} does not match assignment expression type ${expression.returnType}"
        }
    }


    override fun eval(context: LocalRuntimeContext): Any {
        context.instance.setGlobal(global.index, expression.eval(context))
        return Unit
    }

    override fun children() = listOf(expression)

    override fun reconstruct(newChildren: List<Expr>) = GlobalAssignment(global, newChildren[0])

    override val returnType: Type
        get() = Void

    override fun toString(writer: CodeWriter) {
        writer.write("Set(global${global.index}, ")
        writer.write(expression)
        writer.write(')')
    }

    override fun toWasm(writer: WasmWriter) {
        expression.toWasm(writer)
        writer.write(WasmOpcode.GLOBAL_SET)
        writer.writeU32(global.index)
    }
}
package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.binary.WasmOpcode
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.binary.WasmWriter

class LocalReference(
    val index: Int,
    val mutable: Boolean,
    val type: Type
) : Expr() {

    override val returnType: List<Type>
        get() = listOf(type)

    override fun toString(writer: CodeWriter) =
        writer.write("local$index")

    override fun toWasm(writer: WasmWriter) {
        writer.writeOpcode(WasmOpcode.LOCAL_GET)
        writer.writeU32(index)
    }

    fun tee(value: Any): Expr {
        val valueExpr = Expr.of(value)
        require(valueExpr.returnType == listOf(type)) {
            "Value type (${valueExpr.returnType}) does not match variable type ($type)."
        }
        return TeeImpl(valueExpr)
    }

    inner class TeeImpl(expr: Expr) : Expr(expr) {
        override fun toString(writer: CodeWriter) {
            writer.write(this@LocalReference, ".tee(", children.first(), ")")
        }

        override val returnType: List<Type>
            get() = listOf(type)

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(WasmOpcode.LOCAL_TEE)
        }
    }
}
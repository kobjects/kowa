package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.WasmType

class GlobalImpl(
    override val index: Int,
    override val mutable: Boolean,
    val initializer: Expr

) : GlobalInterface {
    override val type: WasmType
        get() = initializer.returnType[0]


    fun toString(writer: CodeWriter) {
        writer.write(if (mutable) "val var$index = Var(" else "val const$index = Const(")
        initializer.toString(writer)
        writer.write(")")
    }

    override fun toString() = if (mutable) "var$index" else "const$index"
}
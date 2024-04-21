package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.binary.Wasm
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.type.Type

class GlobalImpl(
    override val index: Int,
    override val mutable: Boolean,
    override val type: Type,
    val initializer: Wasm

) : GlobalInterface {

    fun toString(writer: CodeWriter) {
        writer.write(if (mutable) "val var$index = Var(" else "val const$index = Const(")
        writer.write(initializer)
        writer.write(")")
    }

    override fun toString() = if (mutable) "var$index" else "const$index"
}
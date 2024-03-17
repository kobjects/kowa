package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Type

class GlobalImpl(
    override val index: Int,
    override val mutable: Boolean,
    val initializer: Node

) : GlobalInterface {
    override val type: Type
        get() = initializer.returnType


    fun toString(writer: CodeWriter) {
        writer.write(if (mutable) "val var$index = Var(" else "val const$index = Const(")
        initializer.toString(writer)
        writer.write(")")
    }

    override fun toString() = if (mutable) "var$index" else "const$index"
}
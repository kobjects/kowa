package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType

interface Callable {
    val index: Int
    val type: FuncType
    val localContextSize: Int

    fun call(context: LocalRuntimeContext): Any

    operator fun invoke(vararg node: Any) =
        Call(this, *node.map { Node.of(it) }.toTypedArray())

    fun toString(writer: CodeWriter)
}
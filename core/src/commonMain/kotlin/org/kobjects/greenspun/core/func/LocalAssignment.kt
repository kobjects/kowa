package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.types.Void
import org.kobjects.greenspun.core.tree.CodeWriter

open class LocalAssignment(
    val index: Int,
    val expression: Node
) : Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        context.setLocal(index, expression.eval(context))
        return Unit
    }

    override fun children() = listOf(expression)

    override fun reconstruct(newChildren: List<Node>) = LocalAssignment(index, newChildren[0])

    override val returnType: Type
        get() = Void

    override fun toString(writer: CodeWriter) {
        writer.write("Set(local$index, ")
        writer.write(expression)
        writer.write(')')
    }
}
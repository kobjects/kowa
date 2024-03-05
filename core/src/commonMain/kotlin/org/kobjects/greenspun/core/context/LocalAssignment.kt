package org.kobjects.greenspun.core.context

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.data.Void

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

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append("Set(local$index, ")
        expression.stringify(sb, indent)
        sb.append(")")
    }
}
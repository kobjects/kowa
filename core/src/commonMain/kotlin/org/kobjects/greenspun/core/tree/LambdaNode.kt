package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type

class LambdaNode(
    private val name: String,
    override val returnType: Type,
    vararg children: Node,
    private val op: (LocalRuntimeContext, List<Node>) -> Any
) : Node() {
    val children = children.toList()

    override fun eval(context: LocalRuntimeContext): Any {
        return op(context, children)
    }

    override fun children() = children

    override fun reconstruct(newChildren: List<Node>) =
        LambdaNode(name, returnType = returnType, children = newChildren.toTypedArray(), op = op)

    override fun toString(writer: CodeWriter) {
        stringifyChildren(writer, "$name(", ", ", ")")
    }
}

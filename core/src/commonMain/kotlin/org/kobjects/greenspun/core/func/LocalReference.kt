package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.Type

class LocalReference(
    val index: Int,
    override val returnType: Type
) : Node() {
    override fun eval(context: LocalRuntimeContext) = context.getLocal(index)

    override fun children(): List<Node> = emptyList()

    override fun reconstruct(newChildren: List<Node>) = this

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append("local$index")
    }
}
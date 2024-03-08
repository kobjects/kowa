package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type

class SimpleNode(
    val name: String,
    override val returnType: Type,
    val operation: (LocalRuntimeContext) -> Any) : LeafNode() {

    override fun eval(context: LocalRuntimeContext) = operation(context)

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append(name)
    }

}
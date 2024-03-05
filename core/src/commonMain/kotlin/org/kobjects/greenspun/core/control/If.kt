package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type

class If(
    vararg val ifThenElse: Node,
) : Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        for (i in ifThenElse.indices step 2) {
            if (i == ifThenElse.size - 1) {
                return ifThenElse[i].eval(context)
            } else if (ifThenElse[i].eval(context) as Boolean) {
                return ifThenElse[i + 1].eval(context)
            }
        }
        return Unit
    }

    override fun children() = ifThenElse.toList()

    override fun reconstruct(newChildren: List<Node>) = If(*newChildren.toTypedArray())

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append("If(")
        sb.append(ifThenElse[0])
        val innerIndent = indent + "  "
        for (i in 1 until ifThenElse.size) {
            sb.append(",$innerIndent")
            ifThenElse[i].stringify(sb, innerIndent)
        }
        sb.append(")")
    }

    override val returnType: Type
        get() = ifThenElse[1].returnType

    override fun toString() ="(if ${ifThenElse.joinToString(" ")})"
}
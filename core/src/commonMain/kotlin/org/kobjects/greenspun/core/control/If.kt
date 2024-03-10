package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.types.Void
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.types.Type

class If(
    val condition: Node,
    val then: Node,
    val otherwise: Node? = null
) : Node() {
    override fun eval(context: LocalRuntimeContext) =
        if (condition.evalBool(context)) then.eval(context) else otherwise?.eval(context) ?: Unit

    override fun children() = if (otherwise == null) listOf(condition, then) else listOf(condition, then, otherwise)

    override fun reconstruct(newChildren: List<Node>) =
        If(newChildren[0], newChildren[1], if (newChildren.size > 2) newChildren[2] else null)

    override fun toString(writer: CodeWriter) {
        writer.write("If(", condition, ", ", then)
        if (otherwise != null) {
            writer.write(", ", otherwise)
        }
        writer.write(")")
    }

    override val returnType: Type
        get() = if (otherwise == null) Void else then.returnType

}
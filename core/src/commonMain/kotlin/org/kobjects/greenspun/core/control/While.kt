package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.data.Void

class While(
    val condition: Node,
    val body: Node
): Node() {
    override fun eval(env: LocalRuntimeContext): Any {
        while (condition.eval(env) as Boolean) {
            val result = body.eval(env)
            if (result is FlowSignal) {
                when (result.kind) {
                    FlowSignal.Kind.BREAK -> break
                    FlowSignal.Kind.CONTINUE -> continue
                    FlowSignal.Kind.RETURN -> return result
                }
            }
        }
        return Unit
    }

    override fun children() = listOf(condition, body)

    override fun reconstruct(newChildren: List<Node>) =
        While(newChildren[0], newChildren[1])

    override val returnType: Type
        get() = Void

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append("While(")
        condition.stringify(sb, indent)
        val innerIndent = "$indent  "
        sb.append(",$innerIndent")
        body.stringify(sb, innerIndent)
    }
}
package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.types.Void
import org.kobjects.greenspun.core.tree.CodeWriter

class While(
    val condition: Node,
    val body: Node
): Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        while (condition.evalBool(context)) {
            val result = body.eval(context)
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

    override fun toString(writer: CodeWriter) {
        writer.write("While(", condition, ", ", body, ")")
    }
}
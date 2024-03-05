package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.context.LocalDefinition
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.data.Void

class Block(
    vararg val statements: Node
): Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        var result: Any = Unit
        for (statement: Node in statements) {
            result = statement.eval(context)
            if (result is FlowSignal) {
                return result
            }
        }
        return result
    }

    override fun children() = statements.asList()

    override fun reconstruct(newChildren: List<Node>) =
        Block(statements = newChildren.toTypedArray())

    override val returnType: Type
        get() = if (statements.isEmpty()) Void else statements.last().returnType

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append("Block {")
        if (statements.isNotEmpty()) {
            val innerIndent = "$indent  "
            for (statement in statements) {
                sb.append(innerIndent)
                if (statement !is LocalDefinition) {
                    sb.append('+')
                }
                statement.stringify(sb, innerIndent)
            }
            sb.append(indent)
        }
        sb.append("}")
    }
}
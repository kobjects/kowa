package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalDefinition
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.data.Void
import org.kobjects.greenspun.core.tree.CodeWriter

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

    override fun toString(writer: CodeWriter) {
        writer.write("Block {")
        if (statements.isNotEmpty()) {
            val innerWriter = writer.indented()
            for (statement in statements) {
                innerWriter.newLine()
                if (statement !is LocalDefinition) {
                    innerWriter.write("+")
                }
                statement.toString(innerWriter)
            }
            writer.newLine()
        }
        writer.write("}")
    }
}
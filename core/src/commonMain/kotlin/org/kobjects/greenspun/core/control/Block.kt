package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalDefinition
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.binary.WasmWriter

class Block(
    vararg val statements: Node
): Node() {

    init {
        for (i in 0 until statements.size - 1) {
            val statement = statements[i]
            require(statement.returnType == Void) {
                "Statement type $i is ${statement.returnType} but all statements in a block (except for the last) must be Void"
            }
        }
    }

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

    override fun toWasm(writer: WasmWriter) =
        statements.forEach { it.toWasm(writer) }
}
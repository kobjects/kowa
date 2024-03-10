package org.kobjects.greenspun.core.tree

abstract class AbstractUnaryOperation(
    val operator: UnaryOperator,
    val operand: Node
) : Node() {

    override fun children() = listOf(operand)

    override fun toString(writer: CodeWriter) =
        writer.write("$operator(", operand, ")")

}
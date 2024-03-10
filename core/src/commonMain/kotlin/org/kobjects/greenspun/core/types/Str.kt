package org.kobjects.greenspun.core.types

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.*


object Str : Type {

    operator fun invoke(value: String) = Const(value)

    override fun createConstant(value: Any) = Const(value as String)

    override fun createBinaryOperation(operator: BinaryOperator, leftOperand: Node, rightOperand: Node): Node {
        return BinaryOperation(operator, leftOperand, rightOperand)
    }

    override fun toString() = "Str"

    class Const(
        val value: String
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) =
            writer.write("Str(\"$value\")")

        override val returnType: Type
            get() = Str
    }

    class BinaryOperation(
        operator: BinaryOperator, leftOperand: Node, rightOperand: Node
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {
        override fun eval(context: LocalRuntimeContext) =
            when (operator) {
                BinaryOperator.PLUS -> leftOperand.eval(context).toString() + rightOperand.eval(context)
                else -> throw UnsupportedOperationException()
            }

        override fun reconstruct(newChildren: List<Node>): Node =
            BinaryOperation(operator, leftOperand, rightOperand)

        override val returnType: Type
            get() = Str

    }
}
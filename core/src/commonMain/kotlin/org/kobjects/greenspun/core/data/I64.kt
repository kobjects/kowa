package org.kobjects.greenspun.core.data

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type

/**
 *  I64 type & builtin operations.
 */
object I64 : Type {

    operator fun invoke(value: Long) = Const(value)

    override fun createConstant(value: Any) = Const(value as Long)

    override fun createNegOperation(operand: Node) = UnaryOperation("-", operand) { -it }

    override fun createInfixOperation(
        operator: Type.InfixOperator,
        leftOperand: Node,
        rightOperand: Node
    ) = InfixOperation(operator, leftOperand, rightOperand)

    override fun createRelationalOperation(
        operator: Type.RelationalOperator,
        leftOperand: Node,
        rightOperand: Node
    ) = RelationalOperation(operator, leftOperand, rightOperand)

    class Const(
        val value: Long
    ): Node() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalI64(context: LocalRuntimeContext) = value

        override fun children() = listOf<Node>()

        override fun reconstruct(newChildren: List<Node>) = this

        override fun stringify(sb: StringBuilder, indent: String) {
            sb.append("I64($value)")
        }

        override val returnType: Type
            get() = I64

        override fun toString() = value.toString()
    }

    class InfixOperation(
        val operator: Type.InfixOperator,
        val leftOperand: Node,
        val rightOperand: Node,
    ) : Node() {

        override fun eval(context: LocalRuntimeContext) = evalI64(context)

        override fun evalI64(context: LocalRuntimeContext): Long {
            val leftValue = leftOperand.evalI64(context)
            val rightValue = rightOperand.evalI64(context)
            return when (operator) {
                Type.InfixOperator.PLUS -> leftValue + rightValue
                Type.InfixOperator.DIV -> leftValue / rightValue
                Type.InfixOperator.TIMES -> leftValue * rightValue
                Type.InfixOperator.MINUS -> leftValue - rightValue
                Type.InfixOperator.REM -> leftValue % rightValue
            }
        }

        override fun children() = listOf(leftOperand, rightOperand)

        override fun reconstruct(newChildren: List<Node>): Node =
            InfixOperation(operator, newChildren[0], newChildren[1])

        override fun stringify(sb: StringBuilder, indent: String) =
            stringifyChildren(sb, indent, "(", " $operator ", ")")

        override val returnType: Type
            get() = I64
    }

    class UnaryOperation(
        private val name: String,
        private val operand: Node,
        private val operation: (Long) -> Long
    ) : Node() {
        override fun eval(context: LocalRuntimeContext): Long =
            operation(operand.evalI64(context))

        override fun evalI64(context: LocalRuntimeContext): Long =
            operation(operand.evalI64(context))

        override fun children() = listOf(operand)

        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(name, newChildren[0], operation)

        override fun stringify(sb: StringBuilder, indent: String) {
            sb.append(name)
            sb.append("(")
            operand.stringify(sb, indent)
            sb.append(")")
        }

        override val returnType: Type
            get() = I64
    }

    class RelationalOperation(
        val operator: Type.RelationalOperator,
        val leftOperand: Node,
        val rightOperand: Node,
    ): Node() {
        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalI64(context)
            val rightValue = rightOperand.evalI64(context)
            return when (operator) {
                Type.RelationalOperator.EQ -> leftValue == rightValue
                Type.RelationalOperator.NE -> leftValue != rightValue
                Type.RelationalOperator.LE -> leftValue <= rightValue
                Type.RelationalOperator.GE -> leftValue >= rightValue
                Type.RelationalOperator.GT -> leftValue > rightValue
                Type.RelationalOperator.LT -> leftValue < rightValue
            }
        }

        override fun children() = listOf(leftOperand, rightOperand)
        override fun reconstruct(newChildren: List<Node>): Node =
            RelationalOperation(operator, newChildren[0], newChildren[1])

        override fun stringify(sb: StringBuilder, indent: String) =
            stringifyChildren(sb, indent, "(", " $operator ", ")")

        override val returnType: Type
            get() = Bool
    }


    override fun toString() = "I64"
}
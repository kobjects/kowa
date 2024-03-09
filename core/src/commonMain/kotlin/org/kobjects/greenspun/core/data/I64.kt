package org.kobjects.greenspun.core.data

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.wasm.WasmOpcode
import org.kobjects.greenspun.core.wasm.WasmWriter

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

        override fun toString(writer: CodeWriter) {
            writer.write("I64($value)")
        }

        override val returnType: Type
            get() = I64
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

        override fun toString(writer: CodeWriter) =
            stringifyChildren(writer, "(", " $operator ", ")")

        override val returnType: Type
            get() = I64

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when (operator) {
                Type.InfixOperator.PLUS -> WasmOpcode.I64_ADD
                Type.InfixOperator.MINUS -> WasmOpcode.I64_SUB
                Type.InfixOperator.TIMES -> WasmOpcode.I64_MUL
                Type.InfixOperator.DIV -> WasmOpcode.I64_DIV_S
                Type.InfixOperator.REM -> WasmOpcode.I64_REM_S
            })
        }
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

        override fun toString(writer: CodeWriter) {
            writer.write("$name(")
            writer.write(operand)
            writer.write(')')
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

        override fun toString(writer: CodeWriter) =
            stringifyChildren(writer, "(", " $operator ", ")")

        override val returnType: Type
            get() = Bool

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                Type.RelationalOperator.EQ -> WasmOpcode.I64_EQ
                Type.RelationalOperator.GE -> WasmOpcode.I64_GE_S
                Type.RelationalOperator.GT -> WasmOpcode.I64_GT_S
                Type.RelationalOperator.LE -> WasmOpcode.I64_LE_S
                Type.RelationalOperator.LT -> WasmOpcode.I64_LT_S
                Type.RelationalOperator.NE -> WasmOpcode.I64_NE
            })
        }
    }


    override fun toString() = "I64"
}
package org.kobjects.greenspun.core.data

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.tree.LeafNode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.wasm.WasmOpcode
import org.kobjects.greenspun.core.wasm.WasmWriter

/**
 * F64 type & builtin operations.
 */
object F64 : Type {

    operator fun invoke(value: Double) = Const(value)

    override fun createConstant(value: Any) = Const(value as Double)

    override fun createInfixOperation(
        operator: Type.InfixOperator,
        leftOperand: Node,
        rightOperand: Node
    ): Node = InfixOperation(operator, leftOperand, rightOperand)

    override fun createRelationalOperation(
        operator: Type.RelationalOperator,
        leftOperand: Node,
        rightOperand: Node
    ): Node = RelationalOperation(operator, leftOperand, rightOperand)

    override fun createNegOperation(operand: Node): Node {
        return UnaryOperation("-", WasmOpcode.F64_NEG, operand) { -it }
    }

    class Const(
        val value: Double
    ): LeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalF64(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) {
            writer.write("F64(")
            writer.write(value.toString())
            writer.write(')')
        }

        override val returnType: Type
            get() = F64
    }

    class InfixOperation(
        val operator: Type.InfixOperator,
        val leftOperand: Node,
        val rightOperand: Node,
    ) : Node() {

        init {
            require(leftOperand.returnType == F64) { "Left operand type must be F64."}
            require(rightOperand.returnType == F64) { "Right operand type must be F64."}
        }

        override val returnType: Type
            get() = F64

        override fun eval(context: LocalRuntimeContext) = evalF64(context)

        override fun evalF64(context: LocalRuntimeContext): Double {
            val leftValue = leftOperand.evalF64(context)
            val rightValue = rightOperand.evalF64(context)
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

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                Type.InfixOperator.PLUS -> WasmOpcode.F64_ADD
                Type.InfixOperator.MINUS -> WasmOpcode.F64_SUB
                Type.InfixOperator.TIMES -> WasmOpcode.F64_MUL
                Type.InfixOperator.DIV -> WasmOpcode.F64_DIV
                Type.InfixOperator.REM -> throw UnsupportedOperationException()
            })
        }

    }


    open class UnaryOperation(
        val name: String,
        val opcode: WasmOpcode,
        val operand: Node,
        val operation: (Double) -> Double,
    ) : Node() {

        init {
            require(operand.returnType == F64) { "Operand type must be F64."}
        }

        override fun eval(context: LocalRuntimeContext) = evalF64(context)

        override fun evalF64(context: LocalRuntimeContext) = operation(operand.evalF64(context))

        override fun children() = listOf(operand)

        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(name, opcode, newChildren[0], operation)
        override fun toString(writer: CodeWriter) {
            writer.write("$name(")
            writer.write(operand)
            writer.write(')')
        }

        override val returnType: Type
            get() = F64

        override fun toWasm(writer: WasmWriter) {
            writer.write(opcode)
        }
    }

    class RelationalOperation(
        val operator: Type.RelationalOperator,
        val leftOperand: Node,
        val rightOperand: Node,
    ): Node() {

        init {
            require(leftOperand.returnType == F64) { "Left operand type must be F64" }
            require(rightOperand.returnType == F64) { "Right operand type must be F64" }
        }

        override val returnType: Type
            get() = Bool

        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalF64(context)
            val rightValue = rightOperand.evalF64(context)
            return when (operator) {
                Type.RelationalOperator.EQ -> leftValue == rightValue
                Type.RelationalOperator.GE -> leftValue >= rightValue
                Type.RelationalOperator.GT -> leftValue > rightValue
                Type.RelationalOperator.LE -> leftValue <= rightValue
                Type.RelationalOperator.NE -> leftValue != rightValue
                Type.RelationalOperator.LT -> leftValue < rightValue
            }
        }

        override fun children() = listOf(leftOperand, rightOperand)
        override fun reconstruct(newChildren: List<Node>): Node =
            RelationalOperation(operator, newChildren[0], newChildren[1])

        override fun toString(writer: CodeWriter) =
            stringifyChildren(writer, "(", " $operator ", ")")

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                Type.RelationalOperator.EQ -> WasmOpcode.F64_EQ
                Type.RelationalOperator.GE -> WasmOpcode.F64_GE
                Type.RelationalOperator.GT -> WasmOpcode.F64_GT
                Type.RelationalOperator.LE -> WasmOpcode.F64_LE
                Type.RelationalOperator.LT -> WasmOpcode.F64_LT
                Type.RelationalOperator.NE -> WasmOpcode.F64_NE
            })
        }
    }

    override fun toString() = "F64"
}
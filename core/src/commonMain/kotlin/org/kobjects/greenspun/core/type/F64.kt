package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.ModuleWriter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.truncate

/**
 * F64 type & builtin operations.
 */
object F64 : Type {

    operator fun invoke(value: Double) = Const(value)

    override fun createConstant(value: Any) = Const(value as Double)

    override fun createBinaryOperation(
        operator: BinaryOperator,
        leftOperand: Node,
        rightOperand: Node
    ): Node = BinaryOperation(operator, leftOperand, rightOperand)

    override fun createRelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node
    ): Node = RelationalOperation(operator, leftOperand, rightOperand)

    override fun createUnaryOperation(operator: UnaryOperator, operand: Node): Node {
        return UnaryOperation(operator, operand)
    }

    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmType.F64)
    }

    override fun toString() = "F64"

    class Const(
        val value: Double
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalF64(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) {
            writer.write("F64(")
            writer.write(value.toString())
            writer.write(')')
        }

        override fun toWasm(writer: ModuleWriter) {
            writer.write(WasmOpcode.F64_CONST)
            writer.writeF64(value)
        }

        override val returnType: Type
            get() = F64
    }

    class BinaryOperation(
        operator: BinaryOperator,
        leftOperand: Node,
        rightOperand: Node,
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {

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
                BinaryOperator.ADD -> leftValue + rightValue
                BinaryOperator.DIV -> leftValue / rightValue
                BinaryOperator.MUL -> leftValue * rightValue
                BinaryOperator.SUB -> leftValue - rightValue
                else -> throw UnsupportedOperationException()
            }
        }

        override fun reconstruct(newChildren: List<Node>): Node =
            BinaryOperation(operator, newChildren[0], newChildren[1])

        override fun toWasm(writer: ModuleWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                BinaryOperator.ADD -> WasmOpcode.F64_ADD
                BinaryOperator.SUB -> WasmOpcode.F64_SUB
                BinaryOperator.MUL -> WasmOpcode.F64_MUL
                BinaryOperator.DIV -> WasmOpcode.F64_DIV
                BinaryOperator.REM -> throw UnsupportedOperationException()
                BinaryOperator.COPYSIGN -> WasmOpcode.F64_COPYSIGN
                BinaryOperator.MIN -> WasmOpcode.F64_MIN
                BinaryOperator.MAX -> WasmOpcode.F64_MAX
                BinaryOperator.AND -> throw UnsupportedOperationException()
                BinaryOperator.OR -> throw UnsupportedOperationException()
                BinaryOperator.XOR -> throw UnsupportedOperationException()
                BinaryOperator.SHL -> throw UnsupportedOperationException()
                BinaryOperator.SHR -> throw UnsupportedOperationException()
                BinaryOperator.ROTL -> throw UnsupportedOperationException()
                BinaryOperator.ROTR -> throw UnsupportedOperationException()
            })
        }
    }


    open class UnaryOperation(
        operator: UnaryOperator,
        operand: Node,
    ) : AbstractUnaryOperation(operator, operand) {

        init {
            require(operand.returnType == F64) { "Operand type must be F64."}
        }

        override fun eval(context: LocalRuntimeContext): Any {
            val value = operand.evalF64(context)
            return when (operator) {
                UnaryOperator.ABS -> throw UnsupportedOperationException()
                UnaryOperator.CEIL -> ceil(value)
                UnaryOperator.CLZ -> throw UnsupportedOperationException()
                UnaryOperator.CTZ -> throw UnsupportedOperationException()
                UnaryOperator.FLOOR -> floor(value)
                UnaryOperator.NEAREST -> throw UnsupportedOperationException()
                UnaryOperator.NEG -> -value
                UnaryOperator.POPCNT -> throw UnsupportedOperationException()
                UnaryOperator.SQRT -> sqrt(value)
                UnaryOperator.TO_I32 -> value.toInt()
                UnaryOperator.TO_I64 -> value.toLong()
                UnaryOperator.TO_F64 -> value
                UnaryOperator.TRUNC -> truncate(value)
                UnaryOperator.NOT -> throw UnsupportedOperationException()
            }
        }


        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(operator, newChildren[0])

        override val returnType: Type
            get() = F64

        override fun toWasm(writer: ModuleWriter) {
            writer.write(when (operator) {
                UnaryOperator.ABS -> WasmOpcode.F64_ABS
                UnaryOperator.CEIL -> WasmOpcode.F64_CEIL
                UnaryOperator.CLZ -> throw UnsupportedOperationException()
                UnaryOperator.CTZ -> throw UnsupportedOperationException()
                UnaryOperator.FLOOR -> WasmOpcode.F64_FLOOR
                UnaryOperator.NEAREST -> WasmOpcode.F64_NEAREST
                UnaryOperator.NEG -> WasmOpcode.F64_NEG
                UnaryOperator.NOT -> throw UnsupportedOperationException()
                UnaryOperator.POPCNT -> throw UnsupportedOperationException()
                UnaryOperator.SQRT -> WasmOpcode.F64_SQRT
                UnaryOperator.TO_I32 -> WasmOpcode.I32_TRUNC_F64_S
                UnaryOperator.TO_I64 -> WasmOpcode.I64_TRUNC_F64_S
                UnaryOperator.TO_F64 -> WasmOpcode.NOP
                UnaryOperator.TRUNC -> WasmOpcode.F64_TRUNC
            })
        }
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node,
    ) : AbstractRelationalOperation(operator, leftOperand, rightOperand) {

        init {
            require(leftOperand.returnType == F64) { "Left operand type must be F64" }
            require(rightOperand.returnType == F64) { "Right operand type must be F64" }
        }

        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalF64(context)
            val rightValue = rightOperand.evalF64(context)
            return when (operator) {
                RelationalOperator.EQ -> leftValue == rightValue
                RelationalOperator.GE -> leftValue >= rightValue
                RelationalOperator.GT -> leftValue > rightValue
                RelationalOperator.LE -> leftValue <= rightValue
                RelationalOperator.LT -> leftValue < rightValue
                RelationalOperator.NE -> leftValue != rightValue
            }
        }

        override fun reconstruct(newChildren: List<Node>): Node =
            RelationalOperation(operator, newChildren[0], newChildren[1])


        override fun toWasm(writer: ModuleWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                RelationalOperator.EQ -> WasmOpcode.F64_EQ
                RelationalOperator.GE -> WasmOpcode.F64_GE
                RelationalOperator.GT -> WasmOpcode.F64_GT
                RelationalOperator.LE -> WasmOpcode.F64_LE
                RelationalOperator.LT -> WasmOpcode.F64_LT
                RelationalOperator.NE -> WasmOpcode.F64_NE
            })
        }
    }
}
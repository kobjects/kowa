package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expression.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
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

        override fun toWasm(writer: WasmWriter) {
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
                BinaryOperator.DIV_S -> leftValue / rightValue
                BinaryOperator.MUL -> leftValue * rightValue
                BinaryOperator.SUB -> leftValue - rightValue
                else -> throw UnsupportedOperationException()
            }
        }

        override fun reconstruct(newChildren: List<Node>): Node =
            BinaryOperation(operator, newChildren[0], newChildren[1])

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                BinaryOperator.ADD -> WasmOpcode.F64_ADD
                BinaryOperator.SUB -> WasmOpcode.F64_SUB
                BinaryOperator.MUL -> WasmOpcode.F64_MUL
                BinaryOperator.DIV_S -> WasmOpcode.F64_DIV
                BinaryOperator.COPYSIGN -> WasmOpcode.F64_COPYSIGN
                BinaryOperator.MIN -> WasmOpcode.F64_MIN
                BinaryOperator.MAX -> WasmOpcode.F64_MAX

                BinaryOperator.AND,
                BinaryOperator.OR,
                BinaryOperator.REM_S,
                BinaryOperator.ROTL,
                BinaryOperator.ROTR,
                BinaryOperator.SHL,
                BinaryOperator.SHR_S,
                BinaryOperator.SHR_U,
                BinaryOperator.REM_U,
                BinaryOperator.DIV_U,
                BinaryOperator.XOR  -> throw UnsupportedOperationException()
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
                UnaryOperator.CEIL -> ceil(value)
                UnaryOperator.DEMOTE -> value.toFloat()
                UnaryOperator.FLOOR -> floor(value)
                UnaryOperator.NEG -> -value
                UnaryOperator.REINTERPRET -> value.toBits()
                UnaryOperator.SQRT -> sqrt(value)
                UnaryOperator.TRUNC -> truncate(value)
                UnaryOperator.TRUNC_TO_I32_S -> value.toInt()
                UnaryOperator.TRUNC_TO_I32_U -> value.toUInt().toInt()
                UnaryOperator.TRUNC_TO_I64_U -> value.toULong().toLong()
                UnaryOperator.TRUNC_TO_I64_S -> value.toLong()

                UnaryOperator.ABS,
                UnaryOperator.CLZ,
                UnaryOperator.CTZ,
                UnaryOperator.CONVERT_TO_F32_S,
                UnaryOperator.CONVERT_TO_F32_U,
                UnaryOperator.CONVERT_TO_F64_S,
                UnaryOperator.CONVERT_TO_F64_U,
                UnaryOperator.EXTEND_S,
                UnaryOperator.EXTEND_U,
                UnaryOperator.NEAREST,
                UnaryOperator.NOT,
                UnaryOperator.POPCNT,
                UnaryOperator.PROMOTE,
                UnaryOperator.WRAP -> throw UnsupportedOperationException()

            }
        }


        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(operator, newChildren[0])

        override fun toWasm(writer: WasmWriter) {
            writer.write(when (operator) {
                UnaryOperator.ABS -> WasmOpcode.F64_ABS
                UnaryOperator.CEIL -> WasmOpcode.F64_CEIL
                UnaryOperator.DEMOTE -> WasmOpcode.F32_DEMOTE_F64
                UnaryOperator.FLOOR -> WasmOpcode.F64_FLOOR
                UnaryOperator.NEAREST -> WasmOpcode.F64_NEAREST
                UnaryOperator.NEG -> WasmOpcode.F64_NEG
                UnaryOperator.REINTERPRET -> WasmOpcode.F64_REINTERPRET_I64
                UnaryOperator.SQRT -> WasmOpcode.F64_SQRT
                UnaryOperator.TRUNC -> WasmOpcode.F64_TRUNC

                UnaryOperator.TRUNC_TO_I32_S -> WasmOpcode.I32_TRUNC_F64_S
                UnaryOperator.TRUNC_TO_I64_S -> WasmOpcode.I64_TRUNC_F64_S
                UnaryOperator.TRUNC_TO_I32_U -> TODO()
                UnaryOperator.TRUNC_TO_I64_U -> TODO()

                UnaryOperator.CLZ,
                UnaryOperator.CTZ,
                UnaryOperator.CONVERT_TO_F32_S,
                UnaryOperator.CONVERT_TO_F64_S,
                UnaryOperator.CONVERT_TO_F32_U,
                UnaryOperator.CONVERT_TO_F64_U,
                UnaryOperator.EXTEND_S,
                UnaryOperator.EXTEND_U,
                UnaryOperator.NOT,
                UnaryOperator.WRAP,
                UnaryOperator.PROMOTE,
                UnaryOperator.POPCNT -> throw UnsupportedOperationException()

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


        override fun toWasm(writer: WasmWriter) {
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
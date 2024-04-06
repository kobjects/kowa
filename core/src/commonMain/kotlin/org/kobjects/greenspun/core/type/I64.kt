package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 *  I64 type & builtin operations.
 */
object I64 : Type {

    operator fun invoke(value: Long) = Const(value)

    override fun createConstant(value: Any) = Const(value as Long)

    override fun createUnaryOperation(operator: UnaryOperator, operand: Expr) = UnaryOperation(operator, operand)

    override fun createBinaryOperation(
        operator: BinaryOperator,
        leftOperand: Expr,
        rightOperand: Expr
    ) = BinaryOperation(operator, leftOperand, rightOperand)

    override fun createRelationalOperation(
        operator: RelationalOperator,
        leftOperand: Expr,
        rightOperand: Expr
    ) = RelationalOperation(operator, leftOperand, rightOperand)

    override fun toString() = "I64"

    override fun toWasm(writer: WasmWriter) =
        writer.write(WasmType.I64)

    class Const(
        val value: Long
    ): AbstractLeafExpr() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalI64(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) =
            writer.write("I64($value)")

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I64_CONST)
            writer.writeI64(value)
        }

        override val returnType: Type
            get() = I64
    }

    class BinaryOperation(
        operator: BinaryOperator,
        leftOperand: Expr,
        rightOperand: Expr,
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {

        init {
            require(operator.typeSupport != TypeSupport.FLOAT_ONLY) {
                "Operator '$operator' not supported for Integer types"
            }

            require(leftOperand.returnType == I64) {
                "Left operand ($leftOperand) type (${leftOperand.returnType} must be I64 for '$operator'"
            }

            require(rightOperand.returnType == I64) {
                "Left operand ($rightOperand) type (${rightOperand.returnType} must be I64 for '$operator'"
            }

        }

        override fun eval(context: LocalRuntimeContext) = evalI64(context)

        override fun evalI64(context: LocalRuntimeContext): Long {
            val leftValue = leftOperand.evalI64(context)
            val rightValue = rightOperand.evalI64(context)
            return when (operator) {
                BinaryOperator.ADD -> leftValue + rightValue
                BinaryOperator.DIV_S -> leftValue / rightValue
                BinaryOperator.DIV_U -> TODO()
                BinaryOperator.MUL -> leftValue * rightValue
                BinaryOperator.SUB -> leftValue - rightValue
                BinaryOperator.REM_S -> leftValue % rightValue
                BinaryOperator.REM_U -> TODO()
                BinaryOperator.AND -> leftValue and rightValue
                BinaryOperator.OR -> leftValue or rightValue
                BinaryOperator.XOR -> leftValue xor rightValue

                BinaryOperator.SHL -> leftValue shl leftValue.toInt()
                BinaryOperator.SHR_S -> leftValue shr rightValue.toInt()
                BinaryOperator.SHR_U -> TODO()

                BinaryOperator.ROTL -> leftValue.rotateLeft((rightValue and 31).toInt())
                BinaryOperator.ROTR -> leftValue.rotateRight((rightValue and 31).toInt())

                BinaryOperator.COPYSIGN -> if (leftValue.sign == rightValue.sign) leftValue else -leftValue
                BinaryOperator.MIN -> min(leftValue, rightValue)
                BinaryOperator.MAX -> max(leftValue, rightValue)
            }
        }

        override fun reconstruct(newChildren: List<Expr>): Expr =
            BinaryOperation(operator, newChildren[0], newChildren[1])

        override val returnType: Type
            get() = I64

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(
                when (operator) {
                    BinaryOperator.ADD -> WasmOpcode.I64_ADD
                    BinaryOperator.SUB -> WasmOpcode.I64_SUB
                    BinaryOperator.MUL -> WasmOpcode.I64_MUL
                    BinaryOperator.DIV_S -> WasmOpcode.I64_DIV_S
                    BinaryOperator.DIV_U -> WasmOpcode.I64_DIV_U
                    BinaryOperator.REM_S -> WasmOpcode.I64_REM_S
                    BinaryOperator.REM_U -> WasmOpcode.I64_REM_U
                    BinaryOperator.AND -> WasmOpcode.I64_AND
                    BinaryOperator.OR -> WasmOpcode.I64_OR
                    BinaryOperator.XOR -> WasmOpcode.I64_XOR
                    BinaryOperator.SHL -> WasmOpcode.I64_SHL
                    BinaryOperator.SHR_S -> WasmOpcode.I64_SHR_S
                    BinaryOperator.SHR_U -> WasmOpcode.I64_SHR_U
                    BinaryOperator.ROTR -> WasmOpcode.I64_ROTR
                    BinaryOperator.ROTL -> WasmOpcode.I64_ROTL

                    BinaryOperator.COPYSIGN,
                    BinaryOperator.MIN,
                    BinaryOperator.MAX -> throw UnsupportedOperationException()
                }
            )
        }
    }

    class UnaryOperation(
        val operator: UnaryOperator,
        val operand: Expr,
    ) : Expr() {
        override fun eval(context: LocalRuntimeContext): Any {
            val value = operand.evalI64(context)
            return when (operator) {
                UnaryOperator.ABS -> throw UnsupportedOperationException()
                UnaryOperator.NEG -> -value
                UnaryOperator.CLZ -> value.countLeadingZeroBits()
                UnaryOperator.CTZ -> value.countTrailingZeroBits()
                UnaryOperator.CONVERT_TO_F32_S -> value.toFloat()
                UnaryOperator.CONVERT_TO_F32_U -> value.toULong().toFloat()
                UnaryOperator.CONVERT_TO_F64_S -> value.toDouble()
                UnaryOperator.CONVERT_TO_F64_U -> value.toULong().toDouble()
                UnaryOperator.NOT -> value.inv()
                UnaryOperator.POPCNT -> value.countOneBits()
                UnaryOperator.REINTERPRET -> Double.fromBits(value)
                UnaryOperator.TRUNC_TO_I32_S -> value.toInt()
                UnaryOperator.TRUNC_TO_I64_S -> value.toLong()
                UnaryOperator.TRUNC_TO_I32_U -> value.toUInt().toInt()
                UnaryOperator.TRUNC_TO_I64_U -> value.toULong().toLong()

                UnaryOperator.CEIL,
                UnaryOperator.DEMOTE,
                UnaryOperator.EXTEND_S,
                UnaryOperator.EXTEND_U,
                UnaryOperator.FLOOR,
                UnaryOperator.NEAREST,
                UnaryOperator.PROMOTE,
                UnaryOperator.SQRT,
                UnaryOperator.TRUNC,
                UnaryOperator.WRAP ->
                    throw UnsupportedOperationException("$operator is unsupported for ${operand.returnType}")

            }
        }


        override fun children() = listOf(operand)

        override fun reconstruct(newChildren: List<Expr>): Expr = UnaryOperation(operator, newChildren[0])

        override fun toString(writer: CodeWriter) =
            writer.write("$operator(", operand, ")")

        override fun toWasm(writer: WasmWriter) {
            if (operator == UnaryOperator.NEG) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeI64(0)
            } else if (operator == UnaryOperator.NOT) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeI64(-1)
            }
            operand.toWasm(writer)
            writer.write(
                when (operator) {
                    UnaryOperator.CLZ -> WasmOpcode.I64_CLZ
                    UnaryOperator.CTZ -> WasmOpcode.I64_CTZ
                    UnaryOperator.POPCNT -> WasmOpcode.I64_POPCNT
                    UnaryOperator.NEG -> WasmOpcode.I64_SUB
                    UnaryOperator.NOT -> WasmOpcode.I64_XOR

                    UnaryOperator.ABS -> throw UnsupportedOperationException()
                    UnaryOperator.CEIL -> throw UnsupportedOperationException()
                    UnaryOperator.FLOOR -> throw UnsupportedOperationException()
                    UnaryOperator.NEAREST -> throw UnsupportedOperationException()
                    UnaryOperator.SQRT -> throw UnsupportedOperationException()
                    UnaryOperator.TRUNC -> throw UnsupportedOperationException()

                    UnaryOperator.EXTEND_S -> TODO()
                    UnaryOperator.EXTEND_U -> TODO()
                    UnaryOperator.TRUNC_TO_I32_S -> TODO()
                    UnaryOperator.TRUNC_TO_I32_U -> TODO()
                    UnaryOperator.TRUNC_TO_I64_U -> TODO()
                    UnaryOperator.TRUNC_TO_I64_S -> TODO()
                    UnaryOperator.WRAP -> TODO()
                    UnaryOperator.PROMOTE -> TODO()
                    UnaryOperator.DEMOTE -> TODO()
                    UnaryOperator.CONVERT_TO_F32_S -> TODO()
                    UnaryOperator.CONVERT_TO_F32_U -> TODO()
                    UnaryOperator.CONVERT_TO_F64_S -> TODO()
                    UnaryOperator.CONVERT_TO_F64_U -> TODO()
                    UnaryOperator.REINTERPRET -> TODO()
                }
            )
        }

        override val returnType: Type
            get() = operator.deviantResultType ?: I64
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Expr,
        rightOperand: Expr,
    ) : AbstractRelationalOperation(operator, leftOperand, rightOperand) {
        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalI64(context)
            val rightValue = rightOperand.evalI64(context)
            return when (operator) {
                RelationalOperator.EQ -> leftValue == rightValue
                RelationalOperator.NE -> leftValue != rightValue
                RelationalOperator.LE -> leftValue <= rightValue
                RelationalOperator.GE -> leftValue >= rightValue
                RelationalOperator.GT -> leftValue > rightValue
                RelationalOperator.LT -> leftValue < rightValue
            }
        }

        override fun reconstruct(newChildren: List<Expr>): Expr =
            RelationalOperation(operator, newChildren[0], newChildren[1])

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(
                when (operator) {
                    RelationalOperator.EQ -> WasmOpcode.I64_EQ
                    RelationalOperator.GE -> WasmOpcode.I64_GE_S
                    RelationalOperator.GT -> WasmOpcode.I64_GT_S
                    RelationalOperator.LE -> WasmOpcode.I64_LE_S
                    RelationalOperator.LT -> WasmOpcode.I64_LT_S
                    RelationalOperator.NE -> WasmOpcode.I64_NE
                }
            )
        }
    }


}
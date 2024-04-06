package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.math.truncate

/**
 * F32 type & builtin operations.
 */
object F32 : Type {

    operator fun invoke(value: Float) = Const(value)

    override fun createConstant(value: Any) = Const(value as Float)

    override fun createBinaryOperation(
        operator: BinaryOperator,
        leftOperand: Expr,
        rightOperand: Expr
    ): Expr = BinaryOperation(operator, leftOperand, rightOperand)

    override fun createRelationalOperation(
        operator: RelationalOperator,
        leftOperand: Expr,
        rightOperand: Expr
    ): Expr = RelationalOperation(operator, leftOperand, rightOperand)

    override fun createUnaryOperation(operator: UnaryOperator, operand: Expr): Expr {
        require(operator.supportedTypes.isEmpty() || operator.supportedTypes.contains(F32)) {
            "Operator $operator not supported for F32"
        }
        return UnaryOperation(operator, operand)
    }

    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmType.F32)
    }

    override fun toString() = "F32"

    class Const(
        val value: Float
    ): AbstractLeafExpr() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalF32(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) {
            writer.write("F32(")
            writer.write(value.toString())
            writer.write(')')
        }

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.F32_CONST)
            writer.writeF32(value)
        }

        override val returnType: Type
            get() = F32
    }

    class BinaryOperation(
        operator: BinaryOperator,
        leftOperand: Expr,
        rightOperand: Expr,
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {

        init {
            require(leftOperand.returnType == F32) { "Left operand type must be F32."}
            require(rightOperand.returnType == F32) { "Right operand type must be F32."}
        }

        override val returnType: Type
            get() = F32

        override fun eval(context: LocalRuntimeContext) = evalF32(context)

        override fun evalF32(context: LocalRuntimeContext): Float {
            val leftValue = leftOperand.evalF32(context)
            val rightValue = rightOperand.evalF32(context)
            return when (operator) {
                BinaryOperator.ADD -> leftValue + rightValue
                BinaryOperator.DIV_S -> leftValue / rightValue
                BinaryOperator.MUL -> leftValue * rightValue
                BinaryOperator.SUB -> leftValue - rightValue
                else -> throw UnsupportedOperationException()
            }
        }

        override fun reconstruct(newChildren: List<Expr>): Expr =
            BinaryOperation(operator, newChildren[0], newChildren[1])

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                BinaryOperator.ADD -> WasmOpcode.F32_ADD
                BinaryOperator.SUB -> WasmOpcode.F32_SUB
                BinaryOperator.MUL -> WasmOpcode.F32_MUL
                BinaryOperator.DIV_S -> WasmOpcode.F32_DIV
                BinaryOperator.COPYSIGN -> WasmOpcode.F32_COPYSIGN
                BinaryOperator.MIN -> WasmOpcode.F32_MIN
                BinaryOperator.MAX -> WasmOpcode.F32_MAX

                BinaryOperator.AND,
                BinaryOperator.REM_S,
                BinaryOperator.OR,
                BinaryOperator.XOR,
                BinaryOperator.SHL,
                BinaryOperator.SHR_S,
                BinaryOperator.ROTL,
                BinaryOperator.ROTR,
                BinaryOperator.DIV_U,
                BinaryOperator.REM_U,
                BinaryOperator.SHR_U -> throw UnsupportedOperationException()
            })
        }
    }


    open class UnaryOperation(
        operator: UnaryOperator,
        operand: Expr,
    ) : AbstractUnaryOperation(operator, operand) {

        init {
            require(operand.returnType == F32) { "Operand type must be F32."}
        }

        override fun eval(context: LocalRuntimeContext): Any {
            val value = operand.evalF32(context)
            return when (operator) {
                UnaryOperator.CEIL -> ceil(value)
                UnaryOperator.FLOOR -> floor(value)
                UnaryOperator.NEG -> -value
                UnaryOperator.SQRT -> sqrt(value)
                UnaryOperator.TRUNC -> truncate(value)

                UnaryOperator.TRUNC_TO_I32_S -> value.toInt()
                UnaryOperator.TRUNC_TO_I64_S -> value.toLong()

                UnaryOperator.TRUNC_TO_I32_U -> value.toUInt().toInt()
                UnaryOperator.TRUNC_TO_I64_U -> value.toULong().toLong()

                UnaryOperator.PROMOTE -> value.toDouble()
                UnaryOperator.REINTERPRET -> value.toBits()

                UnaryOperator.ABS,
                UnaryOperator.CLZ,
                UnaryOperator.CTZ,
                UnaryOperator.CONVERT_TO_F32_S,
                UnaryOperator.CONVERT_TO_F32_U,
                UnaryOperator.CONVERT_TO_F64_U,
                UnaryOperator.CONVERT_TO_F64_S,
                UnaryOperator.DEMOTE,
                UnaryOperator.EXTEND_S,
                UnaryOperator.NEAREST,
                UnaryOperator.NOT,
                UnaryOperator.POPCNT,
                UnaryOperator.EXTEND_U,
                UnaryOperator.WRAP
                        -> throw UnsupportedOperationException()

            }
        }


        override fun reconstruct(newChildren: List<Expr>): Expr = UnaryOperation(operator, newChildren[0])

        override fun toWasm(writer: WasmWriter) {
            writer.write(when (operator) {
                UnaryOperator.ABS -> WasmOpcode.F32_ABS
                UnaryOperator.CEIL -> WasmOpcode.F32_CEIL
                UnaryOperator.FLOOR -> WasmOpcode.F32_FLOOR
                UnaryOperator.NEAREST -> WasmOpcode.F32_NEAREST
                UnaryOperator.NEG -> WasmOpcode.F32_NEG
                UnaryOperator.SQRT -> WasmOpcode.F32_SQRT
                UnaryOperator.TRUNC -> WasmOpcode.F32_TRUNC

                UnaryOperator.TRUNC_TO_I32_S -> WasmOpcode.I32_TRUNC_F32_S
                UnaryOperator.TRUNC_TO_I64_S -> WasmOpcode.I32_TRUNC_F64_S

                UnaryOperator.PROMOTE -> WasmOpcode.F64_PROMOTE_F32
                UnaryOperator.REINTERPRET -> WasmOpcode.I32_REINTERPRET_F32

                UnaryOperator.CLZ,
                UnaryOperator.CTZ,
                UnaryOperator.CONVERT_TO_F32_S,
                UnaryOperator.CONVERT_TO_F64_S,
                UnaryOperator.CONVERT_TO_F32_U,
                UnaryOperator.CONVERT_TO_F64_U,
                UnaryOperator.DEMOTE,
                UnaryOperator.EXTEND_S,
                UnaryOperator.EXTEND_U,
                UnaryOperator.NOT,
                UnaryOperator.TRUNC_TO_I32_U,
                UnaryOperator.TRUNC_TO_I64_U,
                UnaryOperator.WRAP,
                UnaryOperator.POPCNT -> throw UnsupportedOperationException()

            })
        }
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Expr,
        rightOperand: Expr,
    ) : AbstractRelationalOperation(operator, leftOperand, rightOperand) {

        init {
            require(leftOperand.returnType == F32) { "Left operand type must be F32" }
            require(rightOperand.returnType == F32) { "Right operand type must be F32" }
        }

        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalF32(context)
            val rightValue = rightOperand.evalF32(context)
            return when (operator) {
                RelationalOperator.EQ -> leftValue == rightValue
                RelationalOperator.GE -> leftValue >= rightValue
                RelationalOperator.GT -> leftValue > rightValue
                RelationalOperator.LE -> leftValue <= rightValue
                RelationalOperator.LT -> leftValue < rightValue
                RelationalOperator.NE -> leftValue != rightValue
            }
        }

        override fun reconstruct(newChildren: List<Expr>): Expr =
            RelationalOperation(operator, newChildren[0], newChildren[1])


        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                RelationalOperator.EQ -> WasmOpcode.F32_EQ
                RelationalOperator.GE -> WasmOpcode.F32_GE
                RelationalOperator.GT -> WasmOpcode.F32_GT
                RelationalOperator.LE -> WasmOpcode.F32_LE
                RelationalOperator.LT -> WasmOpcode.F32_LT
                RelationalOperator.NE -> WasmOpcode.F32_NE
            })
        }
    }
}
package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.*
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

    override fun createUnaryOperation(operator: UnaryOperator, operand: Node) = UnaryOperation(operator, operand)

    override fun createBinaryOperation(
        operator: BinaryOperator,
        leftOperand: Node,
        rightOperand: Node
    ) = BinaryOperation(operator, leftOperand, rightOperand)

    override fun createRelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node
    ) = RelationalOperation(operator, leftOperand, rightOperand)

    override fun toString() = "I64"

    override fun toWasm(writer: WasmWriter) =
        writer.write(WasmType.I64)

    class Const(
        val value: Long
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalI64(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) =
            writer.write("I64($value)")

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I64_CONST)
            writer.writeVarInt64(value)
        }

        override val returnType: Type
            get() = I64
    }

    class BinaryOperation(
        operator: BinaryOperator,
        leftOperand: Node,
        rightOperand: Node,
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {

        init {
            require(operator.typeSupport != TypeSupport.FLOAT_ONLY) {
                "Operator '$operator' not supported for Integer types"
            }
        }

        override fun eval(context: LocalRuntimeContext) = evalI64(context)

        override fun evalI64(context: LocalRuntimeContext): Long {
            val leftValue = leftOperand.evalI64(context)
            val rightValue = rightOperand.evalI64(context)
            return when (operator) {
                BinaryOperator.ADD -> leftValue + rightValue
                BinaryOperator.DIV -> leftValue / rightValue
                BinaryOperator.MUL -> leftValue * rightValue
                BinaryOperator.SUB -> leftValue - rightValue
                BinaryOperator.REM -> leftValue % rightValue
                BinaryOperator.AND -> leftValue and rightValue
                BinaryOperator.OR -> leftValue or rightValue
                BinaryOperator.XOR -> leftValue xor rightValue

                BinaryOperator.SHL -> leftValue shl leftValue.toInt()
                BinaryOperator.SHR -> leftValue shr rightValue.toInt()

                BinaryOperator.ROTL -> leftValue.rotateLeft((rightValue and 31).toInt())
                BinaryOperator.ROTR -> leftValue.rotateRight((rightValue and 31).toInt())

                BinaryOperator.COPYSIGN -> if (leftValue.sign == rightValue.sign) leftValue else -leftValue
                BinaryOperator.MIN -> min(leftValue, rightValue)
                BinaryOperator.MAX -> max(leftValue, rightValue)
            }
        }

        override fun reconstruct(newChildren: List<Node>): Node =
            BinaryOperation(operator, newChildren[0], newChildren[1])

        override val returnType: Type
            get() = I64

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when (operator) {
                BinaryOperator.ADD -> WasmOpcode.I64_ADD
                BinaryOperator.SUB -> WasmOpcode.I64_SUB
                BinaryOperator.MUL -> WasmOpcode.I64_MUL
                BinaryOperator.DIV -> WasmOpcode.I64_DIV_S
                BinaryOperator.REM -> WasmOpcode.I64_REM_S
                BinaryOperator.AND -> WasmOpcode.I64_AND
                BinaryOperator.OR -> WasmOpcode.I64_OR
                BinaryOperator.XOR -> WasmOpcode.I64_XOR
                BinaryOperator.SHL -> WasmOpcode.I64_SHL
                BinaryOperator.SHR -> WasmOpcode.I64_SHR_S
                BinaryOperator.ROTR -> WasmOpcode.I64_ROTR
                BinaryOperator.ROTL -> WasmOpcode.I64_ROTL
                BinaryOperator.COPYSIGN -> throw UnsupportedOperationException()
                BinaryOperator.MIN -> throw UnsupportedOperationException()
                BinaryOperator.MAX -> throw UnsupportedOperationException()
            })
        }
    }

    class UnaryOperation(
        val operator: UnaryOperator,
        val operand: Node,
    ) : Node() {
        override fun eval(context: LocalRuntimeContext): Any {
            val value = operand.evalI64(context)
            return when (operator) {
                UnaryOperator.ABS -> throw UnsupportedOperationException()
                UnaryOperator.NEG -> -value
                UnaryOperator.CLZ -> value.countLeadingZeroBits()
                UnaryOperator.CTZ -> value.countTrailingZeroBits()
                UnaryOperator.POPCNT -> value.countOneBits()
                UnaryOperator.CEIL -> throw UnsupportedOperationException()
                UnaryOperator.FLOOR -> throw UnsupportedOperationException()
                UnaryOperator.TRUNC -> throw UnsupportedOperationException()
                UnaryOperator.NEAREST -> throw UnsupportedOperationException()
                UnaryOperator.SQRT -> throw UnsupportedOperationException()
                UnaryOperator.TO_I32 -> value.toInt()
                UnaryOperator.TO_I64 -> value
                UnaryOperator.TO_F64 -> value.toDouble()
                UnaryOperator.NOT -> value.inv()
            }
        }


        override fun children() = listOf(operand)

        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(operator, newChildren[0])

        override fun toString(writer: CodeWriter) =
            writer.write("$operator(", operand, ")")

        override fun toWasm(writer: WasmWriter) {
            if (operator == UnaryOperator.NEG) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeVarInt64(0)
            } else if (operator == UnaryOperator.NOT) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeVarInt64(-1)
            }
            operand.toWasm(writer)
            writer.write(when (operator) {
                UnaryOperator.ABS -> throw UnsupportedOperationException()
                UnaryOperator.CEIL -> throw UnsupportedOperationException()
                UnaryOperator.CLZ -> WasmOpcode.I64_CLZ
                UnaryOperator.CTZ -> WasmOpcode.I64_CTZ
                UnaryOperator.FLOOR -> throw UnsupportedOperationException()
                UnaryOperator.POPCNT -> WasmOpcode.I64_POPCNT
                UnaryOperator.NEG -> WasmOpcode.I64_SUB
                UnaryOperator.NEAREST -> throw UnsupportedOperationException()
                UnaryOperator.NOT -> WasmOpcode.I64_XOR
                UnaryOperator.SQRT -> throw UnsupportedOperationException()
                UnaryOperator.TO_I32 -> WasmOpcode.I32_WRAP_I64
                UnaryOperator.TO_I64 -> WasmOpcode.NOP
                UnaryOperator.TO_F64 -> WasmOpcode.F64_CONVERT_I64_S
                UnaryOperator.TRUNC -> throw UnsupportedOperationException()
            })
        }

        override val returnType: Type
            get() = operator.deviantResultType ?: I64
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node,
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

        override fun reconstruct(newChildren: List<Node>): Node =
            RelationalOperation(operator, newChildren[0], newChildren[1])

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                RelationalOperator.EQ -> WasmOpcode.I64_EQ
                RelationalOperator.GE -> WasmOpcode.I64_GE_S
                RelationalOperator.GT -> WasmOpcode.I64_GT_S
                RelationalOperator.LE -> WasmOpcode.I64_LE_S
                RelationalOperator.LT -> WasmOpcode.I64_LT_S
                RelationalOperator.NE -> WasmOpcode.I64_NE
            })
        }
    }


}
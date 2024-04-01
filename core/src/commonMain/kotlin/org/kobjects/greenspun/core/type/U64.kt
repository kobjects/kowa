package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.ModuleWriter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 *  U64 type & builtin operations.
 */
object U64 : Type {

    operator fun invoke(value: ULong) = Const(value)

    override fun createConstant(value: Any) = Const(value as ULong)

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

    override fun toString() = "U64"

    override fun toWasm(writer: WasmWriter) =
        writer.write(WasmType.I64)

    class Const(
        val value: ULong
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalU64(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) =
            writer.write("U64($value)")

        override fun toWasm(writer: ModuleWriter) {
            writer.write(WasmOpcode.I64_CONST)
            writer.writeI64(value as Long)
        }

        override val returnType: Type
            get() = U64
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

        override fun eval(context: LocalRuntimeContext) = evalU64(context)

        override fun evalU64(context: LocalRuntimeContext): ULong {
            val leftValue = leftOperand.evalU64(context)
            val rightValue = rightOperand.evalU64(context)
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

                BinaryOperator.ROTL -> leftValue.rotateLeft((rightValue and 31u).toInt())
                BinaryOperator.ROTR -> leftValue.rotateRight((rightValue and 31u).toInt())

                BinaryOperator.COPYSIGN -> throw UnsupportedOperationException()
                BinaryOperator.MIN -> min(leftValue, rightValue)
                BinaryOperator.MAX -> max(leftValue, rightValue)
            }
        }

        override fun reconstruct(newChildren: List<Node>): Node =
            BinaryOperation(operator, newChildren[0], newChildren[1])

        override val returnType: Type
            get() = U64

        override fun toWasm(writer: ModuleWriter) {
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
            val value = operand.evalU64(context)
            return when (operator) {
                UnaryOperator.ABS -> throw UnsupportedOperationException()
                UnaryOperator.NEG -> throw UnsupportedOperationException()
                UnaryOperator.CLZ -> value.countLeadingZeroBits()
                UnaryOperator.CTZ -> value.countTrailingZeroBits()
                UnaryOperator.POPCNT -> value.countOneBits()
                UnaryOperator.CEIL -> throw UnsupportedOperationException()
                UnaryOperator.FLOOR -> throw UnsupportedOperationException()
                UnaryOperator.TRUNC -> throw UnsupportedOperationException()
                UnaryOperator.NEAREST -> throw UnsupportedOperationException()
                UnaryOperator.SQRT -> throw UnsupportedOperationException()
                UnaryOperator.TO_F32 -> value.toFloat()
                UnaryOperator.TO_F64 -> value.toDouble()
                UnaryOperator.TO_I32 -> value.toInt()
                UnaryOperator.TO_I64 -> value.toLong()
                UnaryOperator.TO_U32 -> value.toUInt()
                UnaryOperator.TO_U64 -> value
                UnaryOperator.NOT -> value.inv()
            }
        }


        override fun children() = listOf(operand)

        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(operator, newChildren[0])

        override fun toString(writer: CodeWriter) =
            writer.write("$operator(", operand, ")")

        override fun toWasm(writer: ModuleWriter) {
            if (operator == UnaryOperator.NEG) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeI64(0)
            } else if (operator == UnaryOperator.NOT) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeI64(-1)
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
                UnaryOperator.TO_F32 -> WasmOpcode.F32_CONVERT_I64_U
                UnaryOperator.TO_F64 -> WasmOpcode.F64_CONVERT_I64_U
                UnaryOperator.TO_I32 -> WasmOpcode.I32_WRAP_I64
                UnaryOperator.TO_I64 -> WasmOpcode.NOP
                UnaryOperator.TO_U32 -> WasmOpcode.I32_WRAP_I64
                UnaryOperator.TO_U64 -> WasmOpcode.NOP
                UnaryOperator.TRUNC -> throw UnsupportedOperationException()
            })
        }

        override val returnType: Type
            get() = operator.deviantResultType ?: U64
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node,
    ) : AbstractRelationalOperation(operator, leftOperand, rightOperand) {
        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalU64(context)
            val rightValue = rightOperand.evalU64(context)
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

        override fun toWasm(writer: ModuleWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when(operator) {
                RelationalOperator.EQ -> WasmOpcode.I64_EQ
                RelationalOperator.GE -> WasmOpcode.I64_GE_U
                RelationalOperator.GT -> WasmOpcode.I64_GT_U
                RelationalOperator.LE -> WasmOpcode.I64_LE_U
                RelationalOperator.LT -> WasmOpcode.I64_LT_U
                RelationalOperator.NE -> WasmOpcode.I64_NE
            })
        }
    }


}
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
 * F32 type & builtin operations.
 */
object F32 : Type {

    operator fun invoke(value: Float) = Const(value)

    override fun createConstant(value: Any) = Const(value as Float)

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
        writer.write(WasmType.F32)
    }

    override fun toString() = "F32"

    class Const(
        val value: Float
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalF32(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) {
            writer.write("F32(")
            writer.write(value.toString())
            writer.write(')')
        }

        override fun toWasm(writer: ModuleWriter) {
            writer.write(WasmOpcode.F32_CONST)
            writer.writeF32(value)
        }

        override val returnType: Type
            get() = F32
    }

    class BinaryOperation(
        operator: BinaryOperator,
        leftOperand: Node,
        rightOperand: Node,
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
                BinaryOperator.ADD -> WasmOpcode.F32_ADD
                BinaryOperator.SUB -> WasmOpcode.F32_SUB
                BinaryOperator.MUL -> WasmOpcode.F32_MUL
                BinaryOperator.DIV -> WasmOpcode.F32_DIV
                BinaryOperator.REM -> throw UnsupportedOperationException()
                BinaryOperator.COPYSIGN -> WasmOpcode.F32_COPYSIGN
                BinaryOperator.MIN -> WasmOpcode.F32_MIN
                BinaryOperator.MAX -> WasmOpcode.F32_MAX
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
            require(operand.returnType == F32) { "Operand type must be F32."}
        }

        override fun eval(context: LocalRuntimeContext): Any {
            val value = operand.evalF32(context)
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
                UnaryOperator.TO_F32 -> value
                UnaryOperator.TO_F64 -> value.toDouble()
                UnaryOperator.TO_I32 -> value.toInt()
                UnaryOperator.TO_I64 -> value.toLong()
                UnaryOperator.TO_U32 -> value.toUInt()
                UnaryOperator.TO_U64 -> value.toULong()
                UnaryOperator.TRUNC -> truncate(value)
                UnaryOperator.NOT -> throw UnsupportedOperationException()
            }
        }


        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(operator, newChildren[0])

        override val returnType: Type
            get() = F32

        override fun toWasm(writer: ModuleWriter) {
            writer.write(when (operator) {
                UnaryOperator.ABS -> WasmOpcode.F32_ABS
                UnaryOperator.CEIL -> WasmOpcode.F32_CEIL
                UnaryOperator.CLZ -> throw UnsupportedOperationException()
                UnaryOperator.CTZ -> throw UnsupportedOperationException()
                UnaryOperator.FLOOR -> WasmOpcode.F32_FLOOR
                UnaryOperator.NEAREST -> WasmOpcode.F32_NEAREST
                UnaryOperator.NEG -> WasmOpcode.F32_NEG
                UnaryOperator.NOT -> throw UnsupportedOperationException()
                UnaryOperator.POPCNT -> throw UnsupportedOperationException()
                UnaryOperator.SQRT -> WasmOpcode.F32_SQRT
                UnaryOperator.TO_F32 -> WasmOpcode.NOP
                UnaryOperator.TO_F64 -> WasmOpcode.F64_PROMOTE_F32
                UnaryOperator.TO_I32 -> WasmOpcode.I32_TRUNC_F32_S
                UnaryOperator.TO_I64 -> WasmOpcode.I64_TRUNC_F32_S
                UnaryOperator.TO_U32 -> WasmOpcode.I32_TRUNC_F32_U
                UnaryOperator.TO_U64 -> WasmOpcode.I64_TRUNC_F32_U
                UnaryOperator.TRUNC -> WasmOpcode.F32_TRUNC
            })
        }
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node,
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

        override fun reconstruct(newChildren: List<Node>): Node =
            RelationalOperation(operator, newChildren[0], newChildren[1])


        override fun toWasm(writer: ModuleWriter) {
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
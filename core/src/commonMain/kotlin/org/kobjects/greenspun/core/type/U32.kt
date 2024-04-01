package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.binary.loadI32
import org.kobjects.greenspun.core.module.ModuleWriter
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 *  U32 type & builtin operations.
 */
object U32 : Type {

    operator fun invoke(value: UInt) = Const(value)

    override fun createConstant(value: Any) = Const(value as UInt)

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

    override fun toString() = "U32"

    override fun toWasm(writer: WasmWriter) = writer.write(WasmType.I32)

    class Const(
        val value: UInt
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalU32(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) =
            writer.write("U32($value)")

        override fun toWasm(writer: ModuleWriter) {
            writer.write(WasmOpcode.I32_CONST)
            writer.writeI32(value.toInt())
        }

        override val returnType: Type
            get() = U32
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

        override fun eval(context: LocalRuntimeContext) = evalU32(context)

        override fun evalU32(context: LocalRuntimeContext): UInt {
            val leftValue = leftOperand.evalU32(context)
            val rightValue = rightOperand.evalU32(context)
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
            get() = U32

        override fun toWasm(writer: ModuleWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(when (operator) {
                BinaryOperator.ADD -> WasmOpcode.I32_ADD
                BinaryOperator.SUB -> WasmOpcode.I32_SUB
                BinaryOperator.MUL -> WasmOpcode.I32_MUL
                BinaryOperator.DIV -> WasmOpcode.I32_DIV_U
                BinaryOperator.REM -> WasmOpcode.I32_REM_U
                BinaryOperator.AND -> WasmOpcode.I32_AND
                BinaryOperator.OR -> WasmOpcode.I32_OR
                BinaryOperator.XOR -> WasmOpcode.I32_XOR
                BinaryOperator.SHL -> WasmOpcode.I32_SHL
                BinaryOperator.SHR -> WasmOpcode.I32_SHR_U
                BinaryOperator.ROTR -> WasmOpcode.I32_ROTR
                BinaryOperator.ROTL -> WasmOpcode.I32_ROTL
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
            val value = operand.evalU32(context)
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
                UnaryOperator.TO_U32 -> value
                UnaryOperator.TO_U64 -> value.toULong()
                UnaryOperator.NOT -> value.inv()
            }
        }


        override fun children() = listOf(operand)

        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(operator, newChildren[0])

        override fun toString(writer: CodeWriter) =
            writer.write("$operator(", operand, ")")

        override fun toWasm(writer: ModuleWriter) {
            if (operator == UnaryOperator.NEG) {
                writer.write(WasmOpcode.I32_CONST)
                writer.writeI32(0)
            } else if (operator == UnaryOperator.NOT) {
                writer.write(WasmOpcode.I32_CONST)
                writer.writeI32(-1)
            }
            operand.toWasm(writer)
            writer.write(when (operator) {
                UnaryOperator.ABS -> throw UnsupportedOperationException()
                UnaryOperator.CEIL -> throw UnsupportedOperationException()
                UnaryOperator.CLZ -> WasmOpcode.I32_CLZ
                UnaryOperator.CTZ -> WasmOpcode.I32_CTZ
                UnaryOperator.FLOOR -> throw UnsupportedOperationException()
                UnaryOperator.POPCNT -> WasmOpcode.I32_POPCNT
                UnaryOperator.NEG -> WasmOpcode.I32_SUB
                UnaryOperator.NEAREST -> throw UnsupportedOperationException()
                UnaryOperator.NOT -> WasmOpcode.I32_XOR
                UnaryOperator.SQRT -> throw UnsupportedOperationException()
                UnaryOperator.TO_I32 -> WasmOpcode.NOP
                UnaryOperator.TO_I64 -> WasmOpcode.I64_EXTEND_I32_U
                UnaryOperator.TO_U32 -> WasmOpcode.NOP
                UnaryOperator.TO_U64 -> WasmOpcode.I64_EXTEND_I32_U
                UnaryOperator.TO_F32 -> WasmOpcode.F32_CONVERT_I32_U
                UnaryOperator.TO_F64 -> WasmOpcode.F64_CONVERT_I32_U
                UnaryOperator.TRUNC -> throw UnsupportedOperationException()
            })
        }

        override val returnType: Type
            get() = operator.deviantResultType ?: U32
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node,
    ) : AbstractRelationalOperation(operator, leftOperand, rightOperand) {
        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalU32(context)
            val rightValue = rightOperand.evalU32(context)
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
                RelationalOperator.EQ -> WasmOpcode.I32_EQ
                RelationalOperator.GE -> WasmOpcode.I32_GE_U
                RelationalOperator.GT -> WasmOpcode.I32_GT_U
                RelationalOperator.LE -> WasmOpcode.I32_LE_U
                RelationalOperator.LT -> WasmOpcode.I32_LT_U
                RelationalOperator.NE -> WasmOpcode.I32_NE
            })
        }
    }


    class Load(val address: Node) : Node() {
        override fun eval(context: LocalRuntimeContext) = context.instance.memory.buffer.loadI32(address.evalI32(context)).toUInt()

        override fun children(): List<Node> = listOf(address)

        override fun reconstruct(newChildren: List<Node>) = Load(newChildren[0])

        override fun toString(writer: CodeWriter) = stringifyChildren(writer, "LoadI32", ", ", ")")

        override fun toWasm(writer: ModuleWriter) {
            writer.write(WasmOpcode.I32_LOAD)
            writer.writeU32(0)
            writer.writeU32(0)
        }

        override val returnType: Type
            get() = U32
    }

}
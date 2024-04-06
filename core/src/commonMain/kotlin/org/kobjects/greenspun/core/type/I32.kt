package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expression.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.binary.loadI32
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 *  I32 type & builtin operations.
 */
object I32 : Type {

    operator fun invoke(value: Int) = Const(value)

    override fun createConstant(value: Any) = Const(value as Int)

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

    override fun toString() = "I32"

    override fun toWasm(writer: WasmWriter) = writer.write(WasmType.I32)

    class Const(
        val value: Int
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalI32(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) =
            writer.write("I32($value)")

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I32_CONST)
            writer.writeI32(value)
        }

        override val returnType: Type
            get() = I32
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

        override fun eval(context: LocalRuntimeContext) = evalI32(context)

        override fun evalI32(context: LocalRuntimeContext): Int {
            val leftValue = leftOperand.evalI32(context)
            val rightValue = rightOperand.evalI32(context)
            return when (operator) {
                BinaryOperator.ADD -> leftValue + rightValue
                BinaryOperator.DIV_S -> leftValue / rightValue
                BinaryOperator.MUL -> leftValue * rightValue
                BinaryOperator.SUB -> leftValue - rightValue
                BinaryOperator.REM_S -> leftValue % rightValue
                BinaryOperator.AND -> leftValue and rightValue
                BinaryOperator.OR -> leftValue or rightValue
                BinaryOperator.XOR -> leftValue xor rightValue

                BinaryOperator.SHL -> leftValue shl leftValue
                BinaryOperator.SHR_S -> leftValue shr rightValue

                BinaryOperator.ROTL -> leftValue.rotateLeft((rightValue and 31).toInt())
                BinaryOperator.ROTR -> leftValue.rotateRight((rightValue and 31).toInt())

                BinaryOperator.MIN -> min(leftValue, rightValue)
                BinaryOperator.MAX -> max(leftValue, rightValue)
                BinaryOperator.COPYSIGN -> if (leftValue.sign == rightValue.sign) leftValue else -leftValue

                BinaryOperator.DIV_U -> (leftValue.toUInt() / rightValue.toUInt()).toInt()
                BinaryOperator.REM_U -> (leftValue.toUInt() % rightValue.toUInt()).toInt()
                BinaryOperator.SHR_U -> leftValue ushr rightValue
            }
        }

        override fun reconstruct(newChildren: List<Node>): Node =
            BinaryOperation(operator, newChildren[0], newChildren[1])

        override val returnType: Type
            get() = I32

        override fun toWasm(writer: WasmWriter) {
            leftOperand.toWasm(writer)
            rightOperand.toWasm(writer)
            writer.write(
                when (operator) {
                    BinaryOperator.ADD -> WasmOpcode.I32_ADD
                    BinaryOperator.SUB -> WasmOpcode.I32_SUB
                    BinaryOperator.MUL -> WasmOpcode.I32_MUL
                    BinaryOperator.DIV_S -> WasmOpcode.I32_DIV_S
                    BinaryOperator.REM_S -> WasmOpcode.I32_REM_S
                    BinaryOperator.AND -> WasmOpcode.I32_AND
                    BinaryOperator.OR -> WasmOpcode.I32_OR
                    BinaryOperator.XOR -> WasmOpcode.I32_XOR
                    BinaryOperator.SHL -> WasmOpcode.I32_SHL
                    BinaryOperator.SHR_S -> WasmOpcode.I32_SHR_S
                    BinaryOperator.ROTR -> WasmOpcode.I32_ROTR
                    BinaryOperator.ROTL -> WasmOpcode.I32_ROTL
                    BinaryOperator.SHR_U -> WasmOpcode.I32_SHR_U
                    BinaryOperator.DIV_U -> WasmOpcode.I32_DIV_U
                    BinaryOperator.REM_U -> WasmOpcode.I32_REM_U
                    BinaryOperator.COPYSIGN -> throw UnsupportedOperationException()
                    BinaryOperator.MIN -> throw UnsupportedOperationException()
                    BinaryOperator.MAX -> throw UnsupportedOperationException()
                }
            )
        }
    }

    class UnaryOperation(
        val operator: UnaryOperator,
        val operand: Node,
    ) : Node() {
        override fun eval(context: LocalRuntimeContext): Any {
            val value = operand.evalI32(context)
            return when (operator) {
                UnaryOperator.CLZ -> value.countLeadingZeroBits()
                UnaryOperator.CTZ -> value.countTrailingZeroBits()
                UnaryOperator.POPCNT -> value.countOneBits()
                UnaryOperator.CONVERT_TO_F64_S -> value.toDouble()
                UnaryOperator.CONVERT_TO_F32_S -> value.toDouble()
                UnaryOperator.CONVERT_TO_F32_U -> value.toUInt().toDouble()
                UnaryOperator.CONVERT_TO_F64_U -> value.toUInt().toDouble()
                UnaryOperator.NOT -> value.inv()
                UnaryOperator.EXTEND_S -> value.toLong()
                UnaryOperator.EXTEND_U -> value.toUInt().toLong()
                UnaryOperator.REINTERPRET -> Float.fromBits(value)

                UnaryOperator.ABS,
                UnaryOperator.NEG,
                UnaryOperator.CEIL,
                UnaryOperator.FLOOR,
                UnaryOperator.TRUNC,
                UnaryOperator.NEAREST,
                UnaryOperator.SQRT,
                UnaryOperator.TRUNC_TO_I32_S,
                UnaryOperator.TRUNC_TO_I64_S,
                UnaryOperator.PROMOTE,
                UnaryOperator.DEMOTE,
                UnaryOperator.TRUNC_TO_I32_U,
                UnaryOperator.TRUNC_TO_I64_U,
                UnaryOperator.WRAP -> throw UnsupportedOperationException()
            }
        }


        override fun children() = listOf(operand)

        override fun reconstruct(newChildren: List<Node>): Node = UnaryOperation(operator, newChildren[0])

        override fun toString(writer: CodeWriter) =
            writer.write("$operator(", operand, ")")

        override fun toWasm(writer: WasmWriter) {
            if (operator == UnaryOperator.NEG) {
                writer.write(WasmOpcode.I32_CONST)
                writer.writeI32(0)
            } else if (operator == UnaryOperator.NOT) {
                writer.write(WasmOpcode.I32_CONST)
                writer.writeI32(-1)
            }
            operand.toWasm(writer)
            writer.write(
                when (operator) {
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

                    UnaryOperator.CONVERT_TO_F64_S -> WasmOpcode.F64_CONVERT_I32_S
                    UnaryOperator.CONVERT_TO_F32_S -> WasmOpcode.F32_CONVERT_I32_S
                    UnaryOperator.CONVERT_TO_F32_U -> WasmOpcode.F32_CONVERT_I32_U
                    UnaryOperator.CONVERT_TO_F64_U -> WasmOpcode.F64_CONVERT_I32_U

                    UnaryOperator.EXTEND_S -> WasmOpcode.I64_EXTEND_I32_S
                    UnaryOperator.EXTEND_U -> WasmOpcode.I64_EXTEND_I32_U
                    UnaryOperator.REINTERPRET -> WasmOpcode.F32_REINTERPRET_I32

                    UnaryOperator.TRUNC,
                    UnaryOperator.TRUNC_TO_I32_S,
                    UnaryOperator.TRUNC_TO_I64_S,
                    UnaryOperator.WRAP,
                    UnaryOperator.PROMOTE,
                    UnaryOperator.DEMOTE,
                    UnaryOperator.TRUNC_TO_I32_U,
                    UnaryOperator.TRUNC_TO_I64_U ->
                        throw UnsupportedOperationException("$operator is unsupported for $I32")
                }
            )
        }

        override val returnType: Type
            get() = operator.deviantResultType ?: I32
    }

    class RelationalOperation(
        operator: RelationalOperator,
        leftOperand: Node,
        rightOperand: Node,
    ) : AbstractRelationalOperation(operator, leftOperand, rightOperand) {
        override fun eval(context: LocalRuntimeContext): Boolean {
            val leftValue = leftOperand.evalI32(context)
            val rightValue = rightOperand.evalI32(context)
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
            writer.write(
                when (operator) {
                    RelationalOperator.EQ -> WasmOpcode.I32_EQ
                    RelationalOperator.GE -> WasmOpcode.I32_GE_S
                    RelationalOperator.GT -> WasmOpcode.I32_GT_S
                    RelationalOperator.LE -> WasmOpcode.I32_LE_S
                    RelationalOperator.LT -> WasmOpcode.I32_LT_S
                    RelationalOperator.NE -> WasmOpcode.I32_NE
                }
            )
        }
    }


    class Load(val address: Node) : Node() {
        override fun eval(context: LocalRuntimeContext) = context.instance.memory.buffer.loadI32(address.evalI32(context))

        override fun children(): List<Node> = listOf(address)

        override fun reconstruct(newChildren: List<Node>) = Load(newChildren[0])

        override fun toString(writer: CodeWriter) = stringifyChildren(writer, "LoadI32", ", ", ")")

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I32_LOAD)
            writer.writeU32(0)
            writer.writeU32(0)
        }

        override val returnType: Type
            get() = I32
    }

}
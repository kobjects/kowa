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
    ): Expr() {
        override fun toString(writer: CodeWriter) =
            writer.write("I64($value)")

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I64_CONST)
            writer.writeI64(value)
        }

        override val returnType: List<Type>
            get() = listOf(I64)
    }

    class BinaryOperation(
        operator: BinaryOperator,
        vararg operands: Expr
    ) : AbstractBinaryOperation(I64, operator, *operands) {

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
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
        operator: UnaryOperator,
        operand: Any,
    ) : AbstractUnaryOperation(I64, operator, operand) {

        override fun toWasm(writer: WasmWriter) {
            if (operator == UnaryOperator.NEG) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeI64(0)
            } else if (operator == UnaryOperator.NOT) {
                writer.write(WasmOpcode.I64_CONST)
                writer.writeI64(-1)
            }
            super.toWasm(writer)
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
    }

    class RelationalOperation(
        operator: RelationalOperator,
        vararg operands: Any,
    ) : AbstractRelationalOperation(I64, operator, *operands) {


        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
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
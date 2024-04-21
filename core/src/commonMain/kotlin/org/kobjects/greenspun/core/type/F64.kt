package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.binary.WasmOpcode
import org.kobjects.greenspun.binary.WasmTypeCode
import org.kobjects.greenspun.binary.WasmWriter

/**
 * F64 type & builtin operations.
 */
object F64 : ValueType {

    operator fun invoke(value: Double) = Const(value)

    override fun createConstant(value: Any) = Const(value as Double)

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
        return UnaryOperation(operator, operand)
    }

    override fun toWasm(writer: WasmWriter) {
        writer.writeTypeCode(WasmTypeCode.F64)
    }

    override fun toString() = "F64"

    class Const(
        val value: Double
    ): Expr() {

        override fun toString(writer: CodeWriter) {
            writer.write("F64(")
            writer.write(value.toString())
            writer.write(')')
        }

        override fun toWasm(writer: WasmWriter) {
            writer.writeOpcode(WasmOpcode.F64_CONST)
            writer.writeF64(value)
        }

        override val returnType: List<org.kobjects.greenspun.core.type.Type>
            get() = listOf(F64)
    }

    class BinaryOperation(
        operator: BinaryOperator,
        vararg operands: Any
    ) : AbstractBinaryOperation(F64, operator, *operands) {


        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(when(operator) {
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
        operand: Any,
    ) : AbstractUnaryOperation(F64, operator, operand) {

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(when (operator) {
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
        vararg operands: Any
    ) : AbstractRelationalOperation(F64, operator, *operands) {


        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(when(operator) {
                RelationalOperator.EQ -> WasmOpcode.F64_EQ
                RelationalOperator.GE -> WasmOpcode.F64_GE
                RelationalOperator.GT -> WasmOpcode.F64_GT
                RelationalOperator.LE -> WasmOpcode.F64_LE
                RelationalOperator.LT -> WasmOpcode.F64_LT
                RelationalOperator.NE -> WasmOpcode.F64_NE
                RelationalOperator.GE_U,
                RelationalOperator.GT_U,
                RelationalOperator.LE_U,
                RelationalOperator.LT_U -> throw UnsupportedOperationException("$operator for F64")
            })
        }
    }
}
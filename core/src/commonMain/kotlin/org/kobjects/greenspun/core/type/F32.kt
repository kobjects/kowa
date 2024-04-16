package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmTypeCode
import org.kobjects.greenspun.core.binary.WasmWriter

/**
 * F32 type & builtin operations.
 */
object F32 : ValueType {

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
        writer.writeTypeCode(WasmTypeCode.F32)
    }

    override fun toString() = "F32"

    class Const(
        val value: Float
    ): Expr() {

        override fun toString(writer: CodeWriter) {
            writer.write("F32(")
            writer.write(value.toString())
            writer.write(')')
        }

        override fun toWasm(writer: WasmWriter) {
            writer.writeOpcode(WasmOpcode.F32_CONST)
            writer.writeF32(value)
        }

        override val returnType: List<org.kobjects.greenspun.core.type.Type>
            get() = listOf(F32)
    }

    class BinaryOperation(
        operator: BinaryOperator,
        vararg children: Any
    ) : AbstractBinaryOperation(F32, operator, *children) {

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(when(operator) {
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
    ) : AbstractUnaryOperation(F32, operator, operand) {


        override fun toWasm(writer: WasmWriter) {
            writer.writeOpcode(when (operator) {
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
        vararg operands: Expr,
    ) : AbstractRelationalOperation(F32, operator, *operands) {


        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(when(operator) {
                RelationalOperator.EQ -> WasmOpcode.F32_EQ
                RelationalOperator.GE -> WasmOpcode.F32_GE
                RelationalOperator.GT -> WasmOpcode.F32_GT
                RelationalOperator.LE -> WasmOpcode.F32_LE
                RelationalOperator.LT -> WasmOpcode.F32_LT
                RelationalOperator.NE -> WasmOpcode.F32_NE
                RelationalOperator.GE_U,
                RelationalOperator.GT_U,
                RelationalOperator.LE_U,
                RelationalOperator.LT_U -> throw UnsupportedOperationException("$operator for F32")
            })
        }
    }
}
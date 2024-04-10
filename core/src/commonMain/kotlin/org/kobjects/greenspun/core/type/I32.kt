package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter

/**
 *  I32 type & builtin operations.
 */
object I32 : Type {

    operator fun invoke(value: Int) = Const(value)

    override fun createConstant(value: Any) = Const(value as Int)

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

    override fun toString() = "I32"

    override fun toWasm(writer: WasmWriter) = writer.write(WasmType.I32)

    class Const(
        val value: Int
    ): Expr() {

        override fun toString(writer: CodeWriter) =
            writer.write("I32($value)")

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I32_CONST)
            writer.writeI32(value)
        }

        override val returnType: List<Type>
            get() = listOf(I32)
    }

    class BinaryOperation(
        operator: BinaryOperator,
        vararg operands: Any,
    ) : AbstractBinaryOperation(I32, operator, *operands) {

        init {
            require(operator.typeSupport != TypeSupport.FLOAT_ONLY) {
                "Operator '$operator' not supported for Integer types"
            }
        }

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
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
        operator: UnaryOperator,
        operand: Any,
    ) : AbstractUnaryOperation(I32, operator, operand) {

        override fun toWasm(writer: WasmWriter) {
            if (operator == UnaryOperator.NEG) {
                writer.write(WasmOpcode.I32_CONST)
                writer.writeI32(0)
            } else if (operator == UnaryOperator.NOT) {
                writer.write(WasmOpcode.I32_CONST)
                writer.writeI32(-1)
            }
            children[0].toWasm(writer)
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

    }

    class RelationalOperation(
        operator: RelationalOperator,
        vararg operands: Any
    ) : AbstractRelationalOperation(I32, operator, *operands) {


        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
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




}
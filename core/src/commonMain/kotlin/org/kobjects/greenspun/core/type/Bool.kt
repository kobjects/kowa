package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter

object Bool : Type {

    val False = Const(false)

    val True = Const(true)



    override fun createConstant(value: Any) = when(value) {
        true -> True
        false -> False
        else -> throw IllegalArgumentException("Not a boolean value: $value")
    }

    override fun createBinaryOperation(operator: BinaryOperator, leftOperand: Expr, rightOperand: Expr): Expr {
        return BinaryOperation(operator, leftOperand, rightOperand)
    }

    override fun createUnaryOperation(operator: UnaryOperator, operand: Expr): Expr {
        return UnaryOperation(operator, operand)
    }

    override fun toWasm(writer: WasmWriter) {
        writer.writeByte(WasmType.I32.code)
    }


    class Const(val value: Boolean) : AbstractLeafExpr() {

        override fun toString(writer: CodeWriter) {
            writer.write(if (value) "True" else "False")
        }

        override val returnType: List<Type>
            get() = listOf(Bool)

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I32_CONST)
            writer.writeI32(if (value) 1 else 0)
        }
    }

    class BinaryOperation(
        operator: BinaryOperator,
        leftOperand: Expr,
        rightOperand: Expr
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {


        override fun toWasm(writer: WasmWriter) {
            when(operator) {
                BinaryOperator.OR -> {
                    leftOperand.toWasm(writer)
                    writer.write(WasmOpcode.IF)
                    Bool.toWasm(writer)
                    writer.write(WasmOpcode.I32_CONST)
                    writer.writeI32(1)
                    writer.write(WasmOpcode.ELSE)
                    rightOperand.toWasm(writer)
                    writer.write(WasmOpcode.END)
                }
                BinaryOperator.AND -> {
                    leftOperand.toWasm(writer)
                    writer.write(WasmOpcode.IF)
                    Bool.toWasm(writer)
                    rightOperand.toWasm(writer)
                    writer.write(WasmOpcode.ELSE)
                    writer.write(WasmOpcode.I32_CONST)
                    writer.writeI32(0)
                    writer.write(WasmOpcode.END)
                }
                else -> throw UnsupportedOperationException()
            }
        }

        override val returnType: List<Type>
            get() = listOf(Bool)
    }




    class UnaryOperation(
        operator: UnaryOperator,
        operand: Expr
    ) : AbstractUnaryOperation(operator, operand) {


        override fun toWasm(writer: WasmWriter) {
            operand.toWasm(writer)
            when (operator) {
                UnaryOperator.NOT -> writer.write(WasmOpcode.I32_EQZ)
                else -> throw UnsupportedOperationException()
            }
        }


    }
}
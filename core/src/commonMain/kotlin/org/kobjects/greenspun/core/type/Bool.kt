package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmTypeCode
import org.kobjects.greenspun.core.binary.WasmWriter

object Bool : WasmType {

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
        writer.writeByte(WasmTypeCode.I32.code)
    }


    override fun toString() = "Bool"

    class Const(val value: Boolean) : Expr() {

        override fun toString(writer: CodeWriter) {
            writer.write(if (value) "True" else "False")
        }

        override val returnType: List<org.kobjects.greenspun.core.type.WasmType>
            get() = listOf(Bool)

        override fun toWasm(writer: WasmWriter) {
            writer.writeOpcode(WasmOpcode.I32_CONST)
            writer.writeI32(if (value) 1 else 0)
        }
    }

    class BinaryOperation(
        operator: BinaryOperator,
        vararg operands: Any
    ) : AbstractBinaryOperation(Bool, operator, *operands) {


        override fun toWasm(writer: WasmWriter) {
            when(operator) {
                BinaryOperator.OR -> {
                    children[0].toWasm(writer)
                    writer.writeOpcode(WasmOpcode.IF)
                    Bool.toWasm(writer)
                    writer.writeOpcode(WasmOpcode.I32_CONST)
                    writer.writeI32(1)
                    writer.writeOpcode(WasmOpcode.ELSE)
                    children[1].toWasm(writer)
                    writer.writeOpcode(WasmOpcode.END)
                }
                BinaryOperator.AND -> {
                    children[0].toWasm(writer)
                    writer.writeOpcode(WasmOpcode.IF)
                    Bool.toWasm(writer)
                    children[1].toWasm(writer)
                    writer.writeOpcode(WasmOpcode.ELSE)
                    writer.writeOpcode(WasmOpcode.I32_CONST)
                    writer.writeI32(0)
                    writer.writeOpcode(WasmOpcode.END)
                }
                else -> throw UnsupportedOperationException()
            }
        }
    }


    class UnaryOperation(
        operator: UnaryOperator,
        operand: Any
    ) : AbstractUnaryOperation(Bool, operator, operand) {


        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            when (operator) {
                UnaryOperator.NOT -> writer.writeOpcode(WasmOpcode.I32_EQZ)
                else -> throw UnsupportedOperationException()
            }
        }


    }
}
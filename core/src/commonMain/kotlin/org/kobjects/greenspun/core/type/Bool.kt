package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.*
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

    override fun createBinaryOperation(operator: BinaryOperator, leftOperand: Node, rightOperand: Node): Node {
        return BinaryOperation(operator, leftOperand, rightOperand)
    }

    override fun createUnaryOperation(operator: UnaryOperator, operand: Node): Node {
        return UnaryOperation(operator, operand)
    }

    override fun toWasm(writer: WasmWriter) {
        writer.writeByte(WasmType.I32.code)
    }


    class Const(val value: Boolean) : AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun evalBool(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) {
            writer.write(if (value) "True" else "False")
        }

        override val returnType: Type
            get() = Bool

        override fun toWasm(writer: WasmWriter) {
            writer.write(WasmOpcode.I32_CONST)
            writer.writeI32(if (value) 1 else 0)
        }
    }

    class BinaryOperation(
        operator: BinaryOperator,
        leftOperand: Node,
        rightOperand: Node
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {
        override fun eval(context: LocalRuntimeContext) = evalBool(context)

        override fun evalBool(context: LocalRuntimeContext) =
            when(operator) {
                BinaryOperator.OR -> if (leftOperand.evalBool(context)) true else rightOperand.evalBool(context)
                BinaryOperator.AND -> if (leftOperand.evalBool(context)) rightOperand.evalBool(context) else false
                else -> throw UnsupportedOperationException()
            }

        override fun reconstruct(newChildren: List<Node>) =
            BinaryOperation(operator, newChildren[0], newChildren[1])

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

        override val returnType: Type
            get() = Bool
    }




    class UnaryOperation(
        operator: UnaryOperator,
        operand: Node
    ) : AbstractUnaryOperation(operator, operand) {
        override fun eval(context: LocalRuntimeContext) =
            when (operator) {
                UnaryOperator.NOT -> !operand.evalBool(context)
                else -> throw UnsupportedOperationException()
            }


        override fun reconstruct(newChildren: List<Node>) =
            UnaryOperation(operator, operand)

        override fun toWasm(writer: WasmWriter) {
            operand.toWasm(writer)
            when (operator) {
                UnaryOperator.NOT -> writer.write(WasmOpcode.I32_EQZ)
                else -> throw UnsupportedOperationException()
            }
        }


    }
}
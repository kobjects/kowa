package org.kobjects.greenspun.core.types

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.*
import org.kobjects.greenspun.core.wasm.WasmOpcode
import org.kobjects.greenspun.core.wasm.WasmWriter

object Bool : Type {

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


    val False = Const(false)

    val True = Const(true)

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
            writer.writeVarInt32(if (value) 1 else 0)
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

        override val returnType: Type
            get() = Bool
    }

    class UnaryOperation(
        operator: UnaryOperator,
        operand: Node
    ) : AbstractUnaryOperation(operator, operand) {
        override fun eval(context: LocalRuntimeContext): Any =
            when (operator) {
                UnaryOperator.NOT -> !operand.evalBool(context)
                else -> throw UnsupportedOperationException()
            }


        override fun reconstruct(newChildren: List<Node>) =
            UnaryOperation(operator, operand)

        override val returnType: Type
            get() = operator.deviantResultType ?: Bool

    }
}
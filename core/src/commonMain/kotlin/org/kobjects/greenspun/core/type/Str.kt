package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expression.*


object Str : Type {

    operator fun invoke(value: String) = Const(value)

    override fun createConstant(value: Any) = Const(value as String)

    override fun createBinaryOperation(operator: BinaryOperator, leftOperand: Node, rightOperand: Node): Node {
        return BinaryOperation(operator, leftOperand, rightOperand)
    }

    override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException("NYI")

    override fun toString() = "Str"

    class Const(
        val value: String
    ): AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun toString(writer: CodeWriter) =
            writer.write("Str(\"$value\")")

        override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException("NYI")

        override val returnType: Type
            get() = Str
    }

    class BinaryOperation(
        operator: BinaryOperator, leftOperand: Node, rightOperand: Node
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {
        override fun eval(context: LocalRuntimeContext) =
            when (operator) {
                BinaryOperator.ADD -> leftOperand.eval(context).toString() + rightOperand.eval(context)
                else -> throw UnsupportedOperationException()
            }

        override fun reconstruct(newChildren: List<Node>): Node =
            BinaryOperation(operator, leftOperand, rightOperand)

        override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException("NYI")

        override val returnType: Type
            get() = Str

    }
}
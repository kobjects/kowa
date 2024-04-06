package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expr.*


object Str : Type {

    operator fun invoke(value: String) = Const(value)

    override fun createConstant(value: Any) = Const(value as String)

    override fun createBinaryOperation(operator: BinaryOperator, leftOperand: Expr, rightOperand: Expr): Expr {
        return BinaryOperation(operator, leftOperand, rightOperand)
    }

    override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException("NYI")

    override fun toString() = "Str"

    class Const(
        val value: String
    ): AbstractLeafExpr() {
        override fun toString(writer: CodeWriter) =
            writer.write("Str(\"$value\")")

        override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException("NYI")

        override val returnType: Type
            get() = Str
    }

    class BinaryOperation(
        operator: BinaryOperator, leftOperand: Expr, rightOperand: Expr
    ) : AbstractBinaryOperation(operator, leftOperand, rightOperand) {

        override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException("NYI")

        override val returnType: Type
            get() = Str

    }
}
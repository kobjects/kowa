package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.Type

abstract class AbstractUnaryOperation(
    val operator: UnaryOperator,
    val operand: Expr
) : Expr() {

    init {
        require(operator.supportedTypes.isEmpty() || operator.supportedTypes.contains(operand.returnType)) {
            "Operator $operator not supported for ${operand.returnType}"
        }
    }

    override final val returnType: Type
        get() = operator.deviantResultType ?: operand.returnType

    override fun children() = listOf(operand)

    override fun toString(writer: CodeWriter) =
        writer.write("$operator(", operand, ")")

}
package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.Type

abstract class AbstractUnaryOperation(
    val operator: UnaryOperator,
    val operand: Expr
) : Expr() {

    init {
        require(operator.supportedTypes.isEmpty() || (operand.returnType.size == 1 && operator.supportedTypes.contains(operand.returnType[0]))) {
            "Operator $operator not supported for ${operand.returnType}"
        }
    }

    override final val returnType: List<Type>
        get() = if (operator.deviantResultType != null) listOf(operator.deviantResultType) else operand.returnType

    override fun children() = listOf(operand)

    override fun toString(writer: CodeWriter) =
        writer.write("$operator(", operand, ")")

}
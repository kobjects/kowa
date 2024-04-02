package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.type.F64
import org.kobjects.greenspun.core.type.Type

abstract class AbstractUnaryOperation(
    val operator: UnaryOperator,
    val operand: Node
) : Node() {

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
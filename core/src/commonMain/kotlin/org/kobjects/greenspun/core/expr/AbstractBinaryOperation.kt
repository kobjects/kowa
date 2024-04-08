package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.Type

abstract class AbstractBinaryOperation(
    val type: Type,
    val operator: BinaryOperator,
    vararg children: Any,
) : Expr(*children) {

    init {
        require(parameterTypes() == listOf(type, type))
    }

    final override fun toString(writer: CodeWriter) =
        when (operator) {
            BinaryOperator.MIN,
            BinaryOperator.MAX,
            BinaryOperator.COPYSIGN ->
                stringifyChildren(writer, "$operator(", ", ", ")")
            else ->
                stringifyChildren(writer, "(", " $operator ", ")")
        }

    final override val returnType: List<Type>
        get() = listOf(type)

}
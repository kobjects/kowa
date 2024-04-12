package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.WasmType

abstract class AbstractBinaryOperation(
    val type: WasmType,
    val operator: BinaryOperator,
    vararg children: Any,
) : Expr(*children) {

    init {
        require(parameterTypes() == listOf(type, type)) {
            "Actual parameter types (${parameterTypes()}) deviate from expectation ${listOf(type, type)} for $operator"
        }
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

    final override val returnType: List<WasmType>
        get() = listOf(type)

}
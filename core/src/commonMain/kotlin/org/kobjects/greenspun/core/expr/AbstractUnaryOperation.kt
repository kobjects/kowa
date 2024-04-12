package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.WasmType

abstract class AbstractUnaryOperation(
    val type: WasmType,
    val operator: UnaryOperator,
    operand: Any
) : Expr(operand) {


    init {
        require(parameterTypes() == listOf(type))

        require(operator.supportedTypes.isEmpty() || operator.supportedTypes.contains(type)) {
            "Operator $operator not supported for ${children[0].returnType}"
        }
    }

    final override val returnType: List<WasmType>
        get() = listOf(if (operator.deviantResultType != null) operator.deviantResultType else type)


    final override fun toString(writer: CodeWriter) =
        writer.write("$operator(", children[0], ")")

}
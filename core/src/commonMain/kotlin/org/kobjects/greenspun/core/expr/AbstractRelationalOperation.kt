package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.WasmType

abstract class AbstractRelationalOperation(
    val type: WasmType,
    val operator: RelationalOperator,
    vararg children: Any
) : Expr(*children) {

    init {
        require(parameterTypes() == listOf(type, type))
    }

    final override val returnType: List<WasmType>
        get() = listOf(Bool)

    final override fun toString(writer: CodeWriter) =
        stringifyChildren(writer, "(", " $operator ", ")")

}
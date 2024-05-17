package org.kobjects.kowa.core.expr

import org.kobjects.kowa.core.type.Bool
import org.kobjects.kowa.core.type.Type

abstract class AbstractRelationalOperation(
    val type: Type,
    val operator: RelationalOperator,
    vararg children: Any
) : Expr(*children) {

    init {
        require(parameterTypes() == listOf(type, type))
    }

    final override val returnType: List<Type>
        get() = listOf(Bool)

    final override fun toString(writer: CodeWriter) =
        stringifyChildren(writer, "(", " $operator ", ")")

}
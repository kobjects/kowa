package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.Type

abstract class AbstractRelationalOperation(
    val operator: RelationalOperator,
    val leftOperand: Expr,
    val rightOperand: Expr
) : Expr() {


    final override fun children() = listOf(leftOperand, rightOperand)
    final override val returnType: List<Type>
        get() = listOf(Bool)

    final override fun toString(writer: CodeWriter) =
        stringifyChildren(writer, "(", " $operator ", ")")

}
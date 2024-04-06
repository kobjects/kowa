package org.kobjects.greenspun.core.expression

import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.Type

abstract class AbstractRelationalOperation(
    val operator: RelationalOperator,
    val leftOperand: Node,
    val rightOperand: Node
) : Node() {


    final override fun children() = listOf(leftOperand, rightOperand)
    final override val returnType: Type
        get() = Bool

    final override fun toString(writer: CodeWriter) =
        stringifyChildren(writer, "(", " $operator ", ")")

}
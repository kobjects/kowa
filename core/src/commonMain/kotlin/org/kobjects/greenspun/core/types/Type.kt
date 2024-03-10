package org.kobjects.greenspun.core.types

import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.tree.BinaryOperator
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.tree.RelationalOperator
import org.kobjects.greenspun.core.tree.UnaryOperator


interface Type {

    fun createConstant(value: Any): Node = throw UnsupportedOperationException()
    fun createBinaryOperation(operator: BinaryOperator, leftOperand: Node, rightOperand: Node): Node = throw UnsupportedOperationException()
    fun createRelationalOperation(operator: RelationalOperator, leftOperand: Node, rightOperand: Node): Node = throw UnsupportedOperationException()
    fun createUnaryOperation(operator: UnaryOperator, operand: Node): Node = throw UnsupportedOperationException()

    companion object {

        fun of(value: Any?) = when (value) {
            null,
            Unit -> Void
            is Long -> I64
            is Double -> F64
            is String -> Str
            is Boolean -> Bool
            is Func -> value.type
            else -> throw IllegalArgumentException("Unrecognized type for $value")
        }
    }
}
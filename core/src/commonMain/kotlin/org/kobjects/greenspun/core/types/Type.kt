package org.kobjects.greenspun.core.types

import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.data.Bool
import org.kobjects.greenspun.core.data.F64
import org.kobjects.greenspun.core.data.I64
import org.kobjects.greenspun.core.data.Str
import org.kobjects.greenspun.core.data.Void
import org.kobjects.greenspun.core.tree.Node


interface Type {

    fun createConstant(value: Any): Node = throw UnsupportedOperationException()
    fun createInfixOperation(operator: InfixOperator, leftOperand: Node, rightOperand: Node): Node = throw UnsupportedOperationException()
    fun createRelationalOperation(operator: RelationalOperator, leftOperand: Node, rightOperand: Node): Node = throw UnsupportedOperationException()
    fun createNegOperation(operand: Node): Node = throw UnsupportedOperationException()

    enum class InfixOperator(val symbol: String) {
        PLUS("+"),
        DIV("/"),
        TIMES("*"),
        MINUS("-"),
        REM("%");

        override fun toString() = symbol
    }

    enum class RelationalOperator {
        EQ, GE, GT, LE, LT, NE;

        override fun toString() = name[0] + name.substring(1).lowercase()
    }

    companion object {

        fun of(value: Any?) = when (value) {
            null -> Void
            is Long -> I64
            is Double -> F64
            is String -> Str
            is Boolean -> Bool
            is Func -> value.type
            else -> throw IllegalArgumentException("Unrecognized type for $value")
        }
    }
}
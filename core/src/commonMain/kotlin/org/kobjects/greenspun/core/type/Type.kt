package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.FuncImpl
import org.kobjects.greenspun.core.expr.BinaryOperator
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.expr.RelationalOperator
import org.kobjects.greenspun.core.expr.UnaryOperator


interface Type {

    fun createConstant(value: Any): Expr = throw UnsupportedOperationException()
    fun createBinaryOperation(operator: BinaryOperator, leftOperand: Expr, rightOperand: Expr): Expr = throw UnsupportedOperationException()
    fun createRelationalOperation(operator: RelationalOperator, leftOperand: Expr, rightOperand: Expr): Expr = throw UnsupportedOperationException()
    fun createUnaryOperation(operator: UnaryOperator, operand: Expr): Expr = throw UnsupportedOperationException()

    fun toWasm(writer: WasmWriter)

    companion object {

        fun of(value: Any?) = when (value) {
            null,
            Unit -> Void
            is Int -> I32
            is Long -> I64
            is Double -> F64
            is String -> Str
            is Boolean -> Bool
            is FuncImpl -> value.type
            else -> throw IllegalArgumentException("Unrecognized type for $value")
        }
    }
}
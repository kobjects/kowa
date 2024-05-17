package org.kobjects.kowa.core.type

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.expr.BinaryOperator
import org.kobjects.kowa.core.expr.Expr
import org.kobjects.kowa.core.expr.RelationalOperator
import org.kobjects.kowa.core.expr.UnaryOperator
import org.kobjects.kowa.core.func.FuncImpl


interface Type {

    fun createConstant(value: Any): Expr = throw UnsupportedOperationException()
    fun createBinaryOperation(operator: BinaryOperator, leftOperand: Expr, rightOperand: Expr): Expr = throw UnsupportedOperationException()
    fun createRelationalOperation(operator: RelationalOperator, leftOperand: Expr, rightOperand: Expr): Expr = throw UnsupportedOperationException()
    fun createUnaryOperation(operator: UnaryOperator, operand: Expr): Expr = throw UnsupportedOperationException()


    fun toWasm(writer: WasmWriter)

    companion object {

        fun of(value: Any?): Type = when (value) {
            is Int -> I32
            is Long -> I64
            is Double -> F64
            is Boolean -> Bool
            is FuncImpl -> value.type
            else -> throw IllegalArgumentException("Unrecognized type for $value")
        }
    }
}
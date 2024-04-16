package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.binary.Wasm
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.Type


abstract class Expr(vararg child: Any) {

    val children: List<Expr> = child.map { of(it) }

    fun parameterTypes() = children.map { it.returnType }.flatten()

    fun toWasm(): Wasm {
        val writer = WasmWriter()
        toWasm(writer)
        return writer.toWasm()
    }

    abstract fun toString(writer: CodeWriter)

    open fun toWasm(writer: WasmWriter) {
        for (child in children) {
            child.toWasm(writer)
        }
    }

    override fun toString(): String {
        val writer = CodeWriter()
        toString(writer)
        return writer.toString()
    }

    fun stringifyChildren(writer: CodeWriter, prefix: String, separator: String = ", ", suffix: String = ")") {
        writer.write(prefix)
        for (i in children.indices) {
            if (i > 0) {
                writer.write(separator)
            }
            children[i].toString(writer)
        }
        writer.write(suffix)
    }

    abstract val returnType: List<Type>

    operator fun plus(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.ADD, this, of(other))
    operator fun minus(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.SUB, this, of(other))
    operator fun times(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.MUL, this, of(other))
    operator fun div(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.DIV_S, this, of(other))
    operator fun rem(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.REM_S, this, of(other))

    infix fun And(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.AND, this, of(other))
    infix fun Or(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.OR, this, of(other))
    infix fun Xor(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.XOR, this, of(other))

    infix fun Shl(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.SHL, this, of(other))
    infix fun Shr(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.SHR_S, this, of(other))

    infix fun ShrU(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.SHR_U, this, of(other))


    infix fun Rotr(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.ROTR, this, of(other))
    infix fun Rotl(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.ROTR, this, of(other))

    infix fun Eq(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.EQ, this, of(other))
    infix fun Ne(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.NE, this, of(other))
    infix fun Ge(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.GE, this, of(other))
    infix fun Gt(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.GT, this, of(other))
    infix fun Le(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.LE, this, of(other))
    infix fun Lt(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.LT, this, of(other))

    infix fun GeU(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.GE_U, this, of(other))
    infix fun GtU(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.GT_U, this, of(other))
    infix fun LeU(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.LE_U, this, of(other))
    infix fun LtU(other: Any) = returnType.first().createRelationalOperation(RelationalOperator.LT_U, this, of(other))


    infix fun DivU(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.DIV_U, this, of(other))
    infix fun RemU(other: Any) = returnType.first().createBinaryOperation(BinaryOperator.REM_U, this, of(other))



    companion object {
        fun of(value: Any): Expr = if (value is Expr) value else Type.of(value).createConstant(value)

        fun Abs(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.ABS, Expr.of(value))
        fun Ceil(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.CEIL, Expr.of(value))
        fun Clz(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.CLZ, Expr.of(value))
        fun Ctz(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.CTZ, Expr.of(value))
        fun Floor(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.FLOOR, Expr.of(value))
        fun Popcnt(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.POPCNT, Expr.of(value))
        fun Neg(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.NEG, Expr.of(value))
        fun Nearest(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.NEAREST, Expr.of(value))
        fun Sqrt(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.SQRT, Expr.of(value))
        fun Trunc(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.TRUNC, Expr.of(value))
        fun Extend(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.EXTEND_S, Expr.of(value))
        fun ExtendU(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.EXTEND_U, Expr.of(value))
        fun TruncToI32(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.TRUNC_TO_I32_S, Expr.of(value))
        fun TruncToI32U(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.TRUNC_TO_I32_U, Expr.of(value))
        fun TruncToI64(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.TRUNC_TO_I64_S, Expr.of(value))
        fun TruncToI64U(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.TRUNC_TO_I64_U, Expr.of(value))
        fun Wrap(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.WRAP, Expr.of(value))
        fun Promote(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.PROMOTE, Expr.of(value))
        fun Demote(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.DEMOTE, Expr.of(value))
        fun ConvertToF32(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.CONVERT_TO_F32_S, Expr.of(value))
        fun ConvertToF32U(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.CONVERT_TO_F32_U, Expr.of(value))
        fun ConvertToF64(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.CONVERT_TO_F64_S, Expr.of(value))
        fun ConvertToF64U(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.CONVERT_TO_F64_U, Expr.of(value))
        fun Reinterpret(value: Any): Expr = Type.of(value).createUnaryOperation(UnaryOperator.REINTERPRET, Expr.of(value))
        fun Max(left: Expr, right: Any) = left.returnType.first().createBinaryOperation(BinaryOperator.MAX, left, of(right))
        fun Min(left: Expr, right: Any) = left.returnType.first().createBinaryOperation(BinaryOperator.MIN, left, of(right))

        fun Not(value: Any): Expr = (if (value is Expr) value.returnType.first() else Type.of(value)).createUnaryOperation(UnaryOperator.NOT, Expr.of(value))

    }
}
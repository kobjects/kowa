package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.runtime.Interpreter
import org.kobjects.greenspun.core.type.Type


abstract class Expr(vararg child: Any) {

    val children: List<Expr> = child.map { of(it) }

    fun parameterTypes() = children.map { it.returnType }.flatten()


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

    operator fun plus(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.ADD, this, of(other))
    operator fun minus(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.SUB, this, of(other))
    operator fun times(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.MUL, this, of(other))
    operator fun div(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.DIV_S, this, of(other))
    operator fun rem(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.REM_S, this, of(other))

    infix fun And(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.AND, this, of(other))
    infix fun Or(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.OR, this, of(other))
    infix fun Xor(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.XOR, this, of(other))

    infix fun Shl(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.SHL, this, of(other))
    infix fun ShrS(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.SHR_S, this, of(other))

    infix fun ShrU(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.SHR_U, this, of(other))


    infix fun Rotr(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.ROTR, this, of(other))
    infix fun Rotl(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.ROTR, this, of(other))

    infix fun Eq(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.EQ, this, of(other))
    infix fun Ne(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.NE, this, of(other))
    infix fun Ge(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.GE, this, of(other))
    infix fun Gt(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.GT, this, of(other))
    infix fun Le(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.LE, this, of(other))
    infix fun Lt(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.LT, this, of(other))

   /* infix fun GeU(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.GE_U, this, of(other))
    infix fun GtU(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.GT, this, of(other))
    infix fun LeU(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.LE, this, of(other))
    infix fun LtU(other: Any) = returnType[0].createRelationalOperation(RelationalOperator.LT, this, of(other))
*/

    fun DivU(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.DIV_U, this, of(other))
    fun RemU(other: Any) = returnType[0].createBinaryOperation(BinaryOperator.REM_U, this, of(other))



    companion object {
        fun of(value: Any): Expr = if (value is Expr) value else Type.of(value).createConstant(value)

        fun Not(value: Any): Expr = (if (value is Expr) value.returnType[0] else Type.of(value)).createUnaryOperation(UnaryOperator.NOT, Expr.of(value))


    }

    fun Max(left: Expr, right: Any) = left.returnType[0].createBinaryOperation(BinaryOperator.MAX, left, of(right))
    fun Min(left: Expr, right: Any) = left.returnType[0].createBinaryOperation(BinaryOperator.MIN, left, of(right))


    fun eval(context: LocalRuntimeContext): Any {
        val wasmWriter = WasmWriter()
        toWasm(wasmWriter)
        val wasm = wasmWriter.toWasm()
        return Interpreter(wasm, context).run()
    }

    fun evalI32(context: LocalRuntimeContext) = eval(context) as Int


}
package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.module.ModuleWriter


abstract class Node {
    abstract fun eval(context: LocalRuntimeContext): Any

    // Override to avoid boxing; use to avoid type casts.
    open fun evalF64(context: LocalRuntimeContext): Double {
        return eval(context) as Double
    }

    open fun evalF32(context: LocalRuntimeContext): Float {
        return eval(context) as Float
    }

    // Override to avoid boxing; use to avoid type casts.
    open fun evalI64(context: LocalRuntimeContext): Long {
        return eval(context) as Long
    }

    open fun evalI32(context: LocalRuntimeContext): Int {
        return eval(context) as Int
    }

    open fun evalBool(context: LocalRuntimeContext): Boolean {
        return eval(context) as Boolean
    }

    abstract fun children(): List<Node>

    abstract fun reconstruct(newChildren: List<Node>): Node

    abstract fun toString(writer: CodeWriter)

    abstract fun toWasm(writer: ModuleWriter)

    override fun toString(): String {
        val writer = CodeWriter()
        toString(writer)
        return writer.toString()
    }

    fun stringifyChildren(writer: CodeWriter, prefix: String, separator: String = ", ", suffix: String = ")") {
        val children = children()
        writer.write(prefix)
        for (i in children.indices) {
            if (i > 0) {
                writer.write(separator)
            }
            children[i].toString(writer)
        }
        writer.write(suffix)
    }

    abstract val returnType: Type

    operator fun plus(other: Any) = returnType.createBinaryOperation(BinaryOperator.ADD, this, of(other))
    operator fun minus(other: Any) = returnType.createBinaryOperation(BinaryOperator.SUB, this, of(other))
    operator fun times(other: Any) = returnType.createBinaryOperation(BinaryOperator.MUL, this, of(other))
    operator fun div(other: Any) = returnType.createBinaryOperation(BinaryOperator.DIV, this, of(other))
    operator fun rem(other: Any) = returnType.createBinaryOperation(BinaryOperator.REM, this, of(other))

    infix fun And(other: Any) = returnType.createBinaryOperation(BinaryOperator.AND, this, of(other))
    infix fun Or(other: Any) = returnType.createBinaryOperation(BinaryOperator.OR, this, of(other))
    infix fun Xor(other: Any) = returnType.createBinaryOperation(BinaryOperator.XOR, this, of(other))

    infix fun Shl(other: Any) = returnType.createBinaryOperation(BinaryOperator.SHR, this, of(other))
    infix fun Shr(other: Any) = returnType.createBinaryOperation(BinaryOperator.SHR, this, of(other))

    infix fun Rotr(other: Any) = returnType.createBinaryOperation(BinaryOperator.ROTR, this, of(other))
    infix fun Rotl(other: Any) = returnType.createBinaryOperation(BinaryOperator.ROTR, this, of(other))

    infix fun Eq(other: Any) = returnType.createRelationalOperation(RelationalOperator.EQ, this, of(other))
    infix fun Ne(other: Any) = returnType.createRelationalOperation(RelationalOperator.NE, this, of(other))
    infix fun Ge(other: Any) = returnType.createRelationalOperation(RelationalOperator.GE, this, of(other))
    infix fun Gt(other: Any) = returnType.createRelationalOperation(RelationalOperator.GT, this, of(other))
    infix fun Le(other: Any) = returnType.createRelationalOperation(RelationalOperator.LE, this, of(other))
    infix fun Lt(other: Any) = returnType.createRelationalOperation(RelationalOperator.LT, this, of(other))



    companion object {
        fun of(value: Any): Node = if (value is Node) value else Type.of(value).createConstant(value)

        fun Not(value: Any): Node = (if (value is Node) value.returnType else Type.of(value)).createUnaryOperation(UnaryOperator.NOT, Node.of(value))


    }

    fun Max(left: Node, right: Any) = left.returnType.createBinaryOperation(BinaryOperator.MAX, left, of(right))
    fun Min(left: Node, right: Any) = left.returnType.createBinaryOperation(BinaryOperator.MIN, left, of(right))
}
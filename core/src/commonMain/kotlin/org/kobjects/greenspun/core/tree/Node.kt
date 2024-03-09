package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.wasm.WasmWriter


abstract class Node {
    abstract fun eval(context: LocalRuntimeContext): Any

    // Override to avoid boxing; use to avoid type casts.
    open fun evalF64(context: LocalRuntimeContext): Double {
        return eval(context) as Double
    }

    // Override to avoid boxing; use to avoid type casts.
    open fun evalI64(context: LocalRuntimeContext): Long {
        return eval(context) as Long
    }

    open fun evalBool(context: LocalRuntimeContext): Boolean {
        return eval(context) as Boolean
    }

    abstract fun children(): List<Node>

    abstract fun reconstruct(newChildren: List<Node>): Node

    abstract fun toString(writer: CodeWriter)

    open fun toWasm(writer: WasmWriter): Unit = throw UnsupportedOperationException()

    override fun toString(): String {
        val writer = CodeWriter()
        toString(writer)
        return writer.toString()
    }

    fun stringifyChildren(writer: CodeWriter, prefix: String, separator: String, suffix: String) {
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

    operator fun plus(other: Any) = returnType.createInfixOperation(Type.InfixOperator.PLUS, this, of(other))
    operator fun minus(other: Any) = returnType.createInfixOperation(Type.InfixOperator.MINUS, this, of(other))
    operator fun times(other: Any) = returnType.createInfixOperation(Type.InfixOperator.TIMES, this, of(other))
    operator fun div(other: Any) = returnType.createInfixOperation(Type.InfixOperator.DIV, this, of(other))
    operator fun rem(other: Any) = returnType.createInfixOperation(Type.InfixOperator.REM, this, of(other))

    infix fun Eq(other: Any) = returnType.createRelationalOperation(Type.RelationalOperator.EQ, this, of(other))
    infix fun Ne(other: Any) = returnType.createRelationalOperation(Type.RelationalOperator.NE, this, of(other))
    infix fun Ge(other: Any) = returnType.createRelationalOperation(Type.RelationalOperator.GE, this, of(other))
    infix fun Gt(other: Any) = returnType.createRelationalOperation(Type.RelationalOperator.GT, this, of(other))
    infix fun Le(other: Any) = returnType.createRelationalOperation(Type.RelationalOperator.LE, this, of(other))
    infix fun Lt(other: Any) = returnType.createRelationalOperation(Type.RelationalOperator.LT, this, of(other))


    companion object {
        fun of(value: Any): Node = if (value is Node) value else Type.of(value).createConstant(value)
    }
}
package org.kobjects.greenspun.core

import kotlin.reflect.KClass


interface Evaluable<C> {
    fun eval(ctx: C): Any?

    fun evalDouble(ctx: C): Double {
        return eval(ctx) as Double
    }

    fun name(): String

    fun children(): List<Evaluable<C>>

    fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C>

    fun type(): Any

    companion object {
        fun <C> sExpression(evaluable: Evaluable<C>): String {
            val sb = StringBuilder("(")
            sb.append(evaluable.name())
            val children = evaluable.children()
            for (child in children) {
                sb.append(' ')
                sb.append(sExpression(child))
            }
            sb.append(')')
            return sb.toString()
        }

    }

}
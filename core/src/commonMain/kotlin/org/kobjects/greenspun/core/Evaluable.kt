package org.kobjects.greenspun.core


interface Evaluable<C> {
    fun eval(ctx: C): Any?

    fun evalDouble(context: C): Double {
        return eval(context) as Double
    }

    fun children(): List<Evaluable<C>>

    fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C>

    val type: Type
}
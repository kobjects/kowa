package org.kobjects.greenspun.core


interface Evaluable<C> {
    fun eval(context: C): Any?

    fun evalF64(context: C): Double {
        return (eval(context) as Number).toDouble()
    }

    fun evalI64(context: C): Long {
        return (eval(context) as Number).toLong()
    }

    fun children(): List<Evaluable<C>>

    fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C>
}
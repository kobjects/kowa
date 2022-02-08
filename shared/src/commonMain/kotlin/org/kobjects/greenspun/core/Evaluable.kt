package org.kobjects.greenspun.core

interface Evaluable<C> {
    fun eval(ctx: C): Any?

    fun evalNumber(ctx: C): Double {
        return eval(ctx) as Double
    }

    fun name(): String

    fun children(): List<Evaluable<C>>
}
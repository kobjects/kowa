package org.kobjects.greenspun

fun interface Evaluable {
    fun eval(env: Env): Any?

    fun evalNumber(env: Env): Double {
        return eval(env) as Double
    }

    companion object {
        val NOOP = Evaluable { null }
    }

}
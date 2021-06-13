package org.kobjects.greenspun.shared

fun interface Evaluable {
    fun eval(environment: Environment): Any?

    companion object {
        val NOOP = Evaluable { null }
    }

}
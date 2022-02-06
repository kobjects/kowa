package org.kobjects.greenspun

class Literal(
    val value: Any?
) : Evaluable {
    override fun eval(env: Env): Any? = value

    companion object {
        val ZERO = Literal(0.0)
        val ONE = Literal(1.0)
    }
}
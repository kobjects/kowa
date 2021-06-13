package org.kobjects.greenspun.shared

class Literal(
    val value: Any?
) : Evaluable {
    override fun eval(environment: Environment): Any? = value
}
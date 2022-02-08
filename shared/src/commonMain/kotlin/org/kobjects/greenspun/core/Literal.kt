package org.kobjects.greenspun.core

class Literal<C, V>(
    val value: V
) : Evaluable<C> {
    override fun eval(ctx: C) = value

    override fun name() = value.toString()

    override fun children() = listOf<Evaluable<C>>()
}
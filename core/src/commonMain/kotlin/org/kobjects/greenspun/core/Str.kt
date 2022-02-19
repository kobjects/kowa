package org.kobjects.greenspun.core


object Str {

    class Const<C>(
        val value: String
    ): Evaluable<C> {
        override fun eval(ctx: C) = value

        override fun children() = listOf<Evaluable<C>>()

        override fun reconstruct(newChildren: List<Evaluable<C>>) = this

        override fun type() = String::class

        override fun toString(indent: String) = "\"$value\""
    }
}
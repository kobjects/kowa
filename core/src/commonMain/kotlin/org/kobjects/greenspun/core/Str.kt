package org.kobjects.greenspun.core


object Str : Type {

    class Const<C>(
        val value: String
    ): Evaluable<C> {
        override fun eval(ctx: C) = value

        override fun children() = listOf<Evaluable<C>>()

        override fun reconstruct(newChildren: List<Evaluable<C>>) = this

        override val type: Type
            get() = Str

        override fun toString() = "\"$value\""
    }

    override val name: String
        get() = "Str"
}
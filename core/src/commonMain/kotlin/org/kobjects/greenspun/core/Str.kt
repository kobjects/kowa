package org.kobjects.greenspun.core


object Str {

    class Const<C>(
        val value: String
    ): Evaluable<C> {
        override fun eval(ctx: C) = value

        override fun children() = listOf<Evaluable<C>>()

        override fun reconstruct(newChildren: List<Evaluable<C>>) = this

        override fun toString() = "\"$value\""
    }

    class Add<C>(
        private val left: Evaluable<C>,
        private val right: Evaluable<C>,
    ) : Evaluable<C> {
        override fun eval(ctx: C) = left.eval(ctx).toString() + right.eval(ctx).toString()

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable<C>>) =
            Add(newChildren[0], newChildren[1])

        override fun toString() = "(+ $left $right)"
    }

    override fun toString() = "Str"
}
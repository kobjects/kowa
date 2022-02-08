package org.kobjects.greenspun.core

/**
 * Relational operations.
 */
object RelationalOp {

    class Eq<E>(
        val left: Evaluable<E>,
        val right: Evaluable<E>,
    ): Evaluable<E> {
        override fun eval(env: E): Boolean {
            return (left.eval(env) == right.eval(env))
        }

        override fun name() = "eq"
        override fun children() = listOf(left, right)
    }

    class Ne<E>(
        val left: Evaluable<E>,
        val right: Evaluable<E>,
    ): Evaluable<E> {
        override fun eval(env: E): Boolean {
            return (left.eval(env) == right.eval(env))
        }

        override fun name() = "ne"
        override fun children() = listOf(left, right)
    }

    class Cmp<E>(
        val name: String,
        val left: Evaluable<E>,
        val right: Evaluable<E>,
        val op: (Double, Double) -> Boolean,
    ) : Evaluable<E> {
        override fun eval(env: E): Boolean =
            op(left.evalNumber(env), right.evalNumber(env))

        override fun name() = name

        override fun children() = listOf(left, right)
    }

    fun <E> Ge(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("ge", left, right) {left, right -> left >= right }

    fun <E> Gt(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("gt", left, right) {left, right -> left > right }

    fun <E> Le(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("le", left, right) {left, right -> left <= right }

    fun <E> Lt(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("lt", left, right) {left, right -> left < right }


}
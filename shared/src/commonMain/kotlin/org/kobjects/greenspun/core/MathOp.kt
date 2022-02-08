package org.kobjects.greenspun.core

/**
 * Operations.
 */
object MathOp {

    class Binary<E>(
        private val name: String,
        private val left: Evaluable<E>,
        private val right: Evaluable<E>,
        private val op: (Double, Double) -> Double
    ) : Evaluable<E> {
        override fun eval(env: E): Double =
            op(left.evalNumber(env), right.evalNumber(env))

        override fun name() = name

        override fun children() = listOf(left, right)
    }

    fun <E> Add(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("add", left, right) { left, right -> left + right }

    fun <E> Mod(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("mod", left, right) { left, right -> left % right }

    fun <E> Sub(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("sub", left, right) { left, right -> left - right }

    fun <E> Mul(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("mul", left, right) { left, right -> left * right }

    fun <E> Div(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("div", left, right) { left, right -> left / right }

}
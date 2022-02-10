package org.kobjects.greenspun.core

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

/**
 * Operations.
 */
object F64 {

    class Const<C>(
        val value: Double
    ): Evaluable<C> {
        override fun eval(ctx: C) = value

        override fun name() = "f64.const:$value"

        override fun children() = listOf<Evaluable<C>>()

        override fun reconstruct(newChildren: List<Evaluable<C>>) = this

        override fun type() = Double::class
    }

    class Binary<E>(
        private val name: String,
        private val left: Evaluable<E>,
        private val right: Evaluable<E>,
        private val op: (Double, Double) -> Double
    ) : Evaluable<E> {
        override fun eval(env: E): Double =
            op(left.evalDouble(env), right.evalDouble(env))

        override fun name() = name

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable<E>>): Evaluable<E> =
            Binary(name, newChildren[0], newChildren[1], op)

        override fun type() = Double::class
    }

    fun <E> add(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("f64.add", left, right) { l, r -> l + r }

    fun <E> mod(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("f64.mod", left, right) { l, r -> l % r }

    fun <E> sub(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("f64.sub", left, right) { l, r -> l - r }

    fun <E> mul(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("f64.mul", left, right) { l, r -> l * r }

    fun <E> div(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("f64.div", left, right) { l, r -> l / r }

    fun <E> pow(left: Evaluable<E>, right: Evaluable<E>) =
        Binary("f64.pow", left, right) { l, r -> l.pow(r) }

    class Unary<E>(
        private val name: String,
        private val arg: Evaluable<E>,
        private val op: (Double) -> Double
    ) : Evaluable<E> {
        override fun eval(env: E): Double =
            op(arg.evalDouble(env))

        override fun name() = name

        override fun children() = listOf(arg)

        override fun reconstruct(newChildren: List<Evaluable<E>>): Evaluable<E> = Unary(name, newChildren[0], op)

        override fun type() = Double::class
    }

    fun <C> ln(arg: Evaluable<C>) = Unary("f64.ln", arg) { ln(it) }
    fun <C> exp(arg: Evaluable<C>) = Unary("f64.exp", arg) { exp(it) }


    class Eq<E>(
        val left: Evaluable<E>,
        val right: Evaluable<E>,
    ): Evaluable<E> {
        override fun eval(env: E): Boolean {
            return (left.eval(env) == right.eval(env))
        }

        override fun name() = "f64.eq"
        override fun children() = listOf(left, right)
        override fun reconstruct(newChildren: List<Evaluable<E>>): Evaluable<E> =
            Eq(newChildren[0], newChildren[1])

        override fun type() = Boolean::class
    }

    class Ne<E>(
        val left: Evaluable<E>,
        val right: Evaluable<E>,
    ): Evaluable<E> {
        override fun eval(env: E): Boolean {
            return (left.evalDouble(env) == right.evalDouble(env))
        }

        override fun name() = "f64.ne"
        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable<E>>): Evaluable<E> =
            Ne(newChildren[0], newChildren[1])

        override fun type() = Boolean::class
    }

    class Cmp<E>(
        val name: String,
        val left: Evaluable<E>,
        val right: Evaluable<E>,
        val op: (Double, Double) -> Boolean,
    ) : Evaluable<E> {
        override fun eval(env: E): Boolean =
            op(left.evalDouble(env), right.evalDouble(env))

        override fun name() = name

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable<E>>): Evaluable<E> =
            Cmp(name, newChildren[0], newChildren[1], op)

        override fun type() = Boolean::class
    }

    fun <E> ge(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("f64.ge", left, right) {left, right -> left >= right }

    fun <E> gt(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("f64.gt", left, right) {left, right -> left > right }

    fun <E> le(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("f64.le", left, right) {left, right -> left <= right }

    fun <E> lt(left: Evaluable<E>, right: Evaluable<E>) =
        Cmp("f64.lt", left, right) {left, right -> left < right }

}
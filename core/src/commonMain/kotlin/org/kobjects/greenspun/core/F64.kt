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

        override fun evalF64(context: C) = value

        override fun children() = listOf<Evaluable<C>>()

        override fun reconstruct(newChildren: List<Evaluable<C>>) = this

        override fun toString() = value.toString()
    }

    open class Binary<C>(
        private val name: String,
        private val left: Evaluable<C>,
        private val right: Evaluable<C>,
        private val op: (Double, Double) -> Double
    ) : Evaluable<C> {

        override fun eval(context: C): Double =
            op(left.evalF64(context), right.evalF64(context))

        override fun evalF64(context: C): Double =
            op(left.evalF64(context), right.evalF64(context))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C> =
            Binary(name, newChildren[0], newChildren[1], op)

        override fun toString() = "($name $left $right)"
    }

    class Add<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Binary<C>("+", left, right, { l, r -> l + r })

    class Mod<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Binary<C>("%", left, right, { l, r -> l % r })

    class Sub<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Binary<C>("-", left, right, { l, r -> l - r })

    class Mul<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Binary<C>("*", left, right, { l, r -> l * r })

    class Div<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Binary<C>("/", left, right, { l, r -> l / r })

    class Pow<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Binary<C>("**", left, right, { l, r -> l.pow(r) })

    open class Unary<C>(
        private val name: String,
        private val arg: Evaluable<C>,
        private val op: (Double) -> Double
    ) : Evaluable<C> {
        override fun eval(ctx: C): Double =
            op(arg.evalF64(ctx))

        override fun evalF64(ctx: C): Double =
            op(arg.evalF64(ctx))

        override fun children() = listOf(arg)

        override fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C> = Unary(name, newChildren[0], op)

        override fun toString(): String = "($name $arg)"
    }

    class Ln<C>(arg: Evaluable<C>) : Unary<C>("ln", arg, { ln(it) })
    class Exp<C>(arg: Evaluable<C>) : Unary<C>("exp", arg, { exp(it) })
    class Neg<C>(arg: Evaluable<C>) : Unary<C>("neg", arg, { -it })


    class Eq<C>(
        val left: Evaluable<C>,
        val right: Evaluable<C>,
    ): Evaluable<C> {
        override fun eval(context: C): Boolean {
            return (left.evalF64(context) == right.evalF64(context))
        }

        override fun children() = listOf(left, right)
        override fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C> =
            Eq(newChildren[0], newChildren[1])

        override fun toString() =
            "(= $left $right)"
    }

    class Ne<C>(
        val left: Evaluable<C>,
        val right: Evaluable<C>,
    ): Evaluable<C> {
        override fun eval(context: C): Boolean {
            return (left.evalF64(context) == right.evalF64(context))
        }

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C> =
            Ne(newChildren[0], newChildren[1])

        override fun toString() = "(!= $left $right)"
    }

    open class Cmp<C>(
        val name: String,
        val left: Evaluable<C>,
        val right: Evaluable<C>,
        val op: (Double, Double) -> Boolean,
    ) : Evaluable<C> {
        override fun eval(env: C): Boolean =
            op(left.evalF64(env), right.evalF64(env))

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Evaluable<C>>): Evaluable<C> =
            Cmp(name, newChildren[0], newChildren[1], op)

        override fun toString() =
            "($name $left $right)"
    }

    class Ge<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Cmp<C>(">=", left, right, {left, right -> left >= right })

    class Gt<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Cmp<C>(">", left, right, {left, right -> left > right })

    class Le<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Cmp<C>("<=", left, right, {left, right -> left <= right })

    class Lt<C>(left: Evaluable<C>, right: Evaluable<C>) :
        Cmp<C>("<", left, right, {left, right -> left < right })

    override fun toString() = "F64"
}
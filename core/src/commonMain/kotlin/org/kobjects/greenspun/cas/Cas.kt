package org.kobjects.greenspun.cas

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.F64.Const
import org.kobjects.greenspun.core.F64.Add

fun <C> Evaluable<C>.isConst(value: Double) = this is Const<*>
        && (this as Const<*>).value == value

fun <C> Evaluable<C>.doubleValue(): Double = (this as Const<*>).value

fun <C> simplify(node: Evaluable<C>): Evaluable<C> {
    val simplified = node.children().map { simplify(it) }

    return when (node) {
        is F64.Add ->
            if (simplified[0] is Const && simplified[1] is Const) {
                Const(simplified[0].doubleValue() + simplified[1].doubleValue())
            } else if (simplified[0].isConst(0.0)) {
                simplified[1]
            } else if (simplified[1].isConst(0.0)) {
                simplified[0]
            } else {
                Add(simplified[0], simplified[1])
            }
        is F64.Sub ->
            if (simplified[0] is Const && simplified[1] is Const) {
                Const(simplified[0].doubleValue() - simplified[1].doubleValue())
            } else if (simplified[1].isConst(0.0)) {
                simplified[0]
            } else {
                F64.Sub(simplified[0], simplified[1])
            }
        is F64.Mul ->
            if (simplified[0] is Const && simplified[1] is Const) {
                Const(simplified[0].doubleValue()
                        * simplified[1].doubleValue())
            } else if (simplified[0].isConst(0.0)
                || simplified[1].isConst(0.0)) {
                Const(0.0)
            } else if (simplified[0].isConst(1.0)) {
                simplified[1]
            } else if (simplified[1].isConst(1.0)) {
                simplified[0]
            } else {
                F64.Mul(simplified[0], simplified[1])
            }
        is F64.Div ->
            if (simplified[0] is Const && simplified[1] is Const) {
                Const(simplified[0].doubleValue() / simplified[1].doubleValue())
            } else if (simplified[0].isConst(0.0)) {
                Const(0.0)
            } else if (simplified[1].isConst(1.0)) {
                simplified[0]
            } else {
                F64.Div(simplified[0], simplified[1])
            }
        is F64.Ln ->
            if (simplified[0].isConst(1.0)) {
                Const(0.0)
            } else {
                F64.Ln(simplified[0])
            }
        is F64.Exp ->
            if (simplified[0].isConst(0.0)) {
                Const(1.0)
            } else {
                F64.Exp(simplified[0])
            }
        else -> node.reconstruct(simplified)
    }
}

fun <C> derive(node: Evaluable<C>): Evaluable<C> {
    val children = node.children()
    return simplify(when (node) {
        is F64.Add -> F64.Add(derive(children[0]), derive(children[1]))
        is F64.Sub -> F64.Sub(derive(children[0]), derive(children[1]))
        is F64.Mul -> F64.Add(
            F64.Mul(derive(children[0]), children[1]),
            F64.Mul(children[0], derive(children[1])))
        is F64.Div -> F64.Div(
            F64.Add(
                F64.Mul(derive(children[0]), children[1]),
            F64.Mul(children[0], derive(children[1]))),
            F64.Mul(children[1], children[1]))
        is F64.Pow -> derive(F64.Exp(F64.Mul(children[0], F64.Ln(children[1]))))
        is F64.Exp -> F64.Mul(node, derive(children[0]))
        is F64.Ln -> F64.Mul(F64.Div(Const(1.0), node), derive(children[0]))
        else -> {
            if (node is Const) {
                return Const(0.0)
            }
            throw IllegalArgumentException("Don't know how to derive $node")
        }
    })
}
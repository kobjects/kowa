package org.kobjects.greenspun.cas

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.F64.Const
import org.kobjects.greenspun.core.F64.add

fun <C> Evaluable<C>.isConst(value: Double) = this is Const<*>
        && (this as Const<*>).value == value

fun <C> Evaluable<C>.doubleValue(): Double = (this as Const<*>).value

fun <C> simplify(node: Evaluable<C>): Evaluable<C> {
    val simplified = node.children().map { simplify(it) }

    return when (node.name()) {
        "f64.add" ->
            if (simplified[0] is Const && simplified[1] is Const) {
                Const(simplified[0].doubleValue() + simplified[1].doubleValue())
            } else if (simplified[0].isConst(0.0)) {
                simplified[1]
            } else if (simplified[1].isConst(0.0)) {
                simplified[0]
            } else {
                add(simplified[0], simplified[1])
            }
        "f64.sub" ->
            if (simplified[0] is Const && simplified[1] is Const) {
                Const(simplified[0].doubleValue() - simplified[1].doubleValue())
            } else if (simplified[1].isConst(0.0)) {
                simplified[0]
            } else {
                F64.sub(simplified[0], simplified[1])
            }
        "f64.mul" ->
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
                F64.mul(simplified[0], simplified[1])
            }
        "f64.div" ->
            if (simplified[0] is Const && simplified[1] is Const) {
                Const(simplified[0].doubleValue() / simplified[1].doubleValue())
            } else if (simplified[0].isConst(0.0)) {
                Const(0.0)
            } else if (simplified[1].isConst(1.0)) {
                simplified[0]
            } else {
                F64.div(simplified[0], simplified[1])
            }
        "f64.ln" ->
            if (simplified[0].isConst(1.0)) {
                Const(0.0)
            } else {
                F64.ln(simplified[0])
            }
        "f64.exp" ->
            if (simplified[0].isConst(0.0)) {
                Const(1.0)
            } else {
                F64.exp(simplified[0])
            }
        else -> node.reconstruct(simplified)
    }
}

fun <C> derive(node: Evaluable<C>): Evaluable<C> {
    val children = node.children()
    return simplify(when (node.name()) {
        "f64.add" -> F64.add(derive(children[0]), derive(children[1]))
        "f64.sub" -> F64.sub(derive(children[0]), derive(children[1]))
        "f64.mul" -> F64.add(
            F64.mul(derive(children[0]), children[1]),
            F64.mul(children[0], derive(children[1])))
        "f64.div" -> F64.div(
            F64.add(
                F64.mul(derive(children[0]), children[1]),
            F64.mul(children[0], derive(children[1]))),
            F64.mul(children[1], children[1]))
        "f64.pow" -> derive(F64.exp(F64.mul(children[0], F64.ln(children[1]))))
        "f64.exp" -> F64.mul(node, derive(children[0]))
        "f64.ln" -> F64.mul(F64.div(Const(1.0), node), derive(children[0]))
        else -> {
            if (node is Const) {
                return Const(0.0)
            }
            throw IllegalArgumentException("Don't know how to derive $node")
        }
    })
}
package org.kobjects.greenspun

import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Evaluable.Companion.sExpression
import org.kobjects.greenspun.core.F64
import org.kobjects.greenspun.core.F64.add
import org.kobjects.greenspun.core.F64.div
import org.kobjects.greenspun.core.F64.exp
import org.kobjects.greenspun.core.F64.ln
import org.kobjects.greenspun.core.F64.mul
import org.kobjects.greenspun.core.F64.sub
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeTests {


    fun <C> Evaluable<C>.isConst(value: Double) = this is F64.Const<*>
            && (this as F64.Const<*>).value == value

    fun <C> Evaluable<C>.doubleValue(): Double = (this as F64.Const<*>).value

    fun <C> simplify(node: Evaluable<C>): Evaluable<C> {
        val simplified = node.children().map { simplify(it) }

        return when (node.name()) {
            "f64.add" ->
                if (simplified[0] is F64.Const && simplified[1] is F64.Const) {
                        F64.Const(simplified[0].doubleValue() + simplified[1].doubleValue())
                } else if (simplified[0].isConst(0.0)) {
                    simplified[1]
                } else if (simplified[1].isConst(0.0)) {
                    simplified[0]
                } else {
                    add(simplified[0], simplified[1])
                }
            "f64.sub" ->
                if (simplified[0] is F64.Const && simplified[1] is F64.Const) {
                    F64.Const(simplified[0].doubleValue() - simplified[1].doubleValue())
                } else if (simplified[1].isConst(0.0)) {
                    simplified[0]
                } else {
                    sub(simplified[0], simplified[1])
                }
            "f64.mul" ->
                if (simplified[0] is F64.Const && simplified[1] is F64.Const) {
                    F64.Const(simplified[0].doubleValue()
                            * simplified[1].doubleValue())
                } else if (simplified[0].isConst(0.0)
                            || simplified[1].isConst(0.0)) {
                                F64.Const(0.0)
                } else if (simplified[0].isConst(1.0)) {
                    simplified[1]
                } else if (simplified[1].isConst(1.0)) {
                    simplified[0]
                } else {
                    mul(simplified[0], simplified[1])
                }
            "f64.div" ->
                if (simplified[0] is F64.Const && simplified[1] is F64.Const) {
                    F64.Const(simplified[0].doubleValue() / simplified[1].doubleValue())
                } else if (simplified[0].isConst(0.0)) {
                       F64.Const(0.0)
                } else if (simplified[1].isConst(1.0)) {
                    simplified[0]
                } else {
                    div(simplified[0], simplified[1])
                }
            "f64.ln" ->
                if (simplified[0].isConst(1.0)) {
                    F64.Const(0.0)
                } else {
                    ln(simplified[0])
                }
            "f64.exp" ->
                if (simplified[0].isConst(0.0)) {
                    F64.Const(1.0)
                } else {
                    exp(simplified[0])
                }
            else -> node.reconstruct(simplified)
        }
    }

    fun <C> derive(node: Evaluable<C>): Evaluable<C> {
        val children = node.children()
        return simplify(when (node.name()) {
            "f64.add" -> add(derive(children[0]), derive(children[1]))
            "f64.sub" -> sub(derive(children[0]), derive(children[1]))
            "f64.mul" -> add(mul(derive(children[0]), children[1]),
                        mul(children[0], derive(children[1])))
            "f64.div" -> div(add(mul(derive(children[0]), children[1]),
                        mul(children[0], derive(children[1]))),
                        mul(children[1], children[1]))
            "f64.pow" -> derive(exp(mul(children[0], ln(children[1]))))
            "f64.exp" -> mul(node, derive(children[0]))
            "f64.ln" -> mul(div(F64.Const(1.0), node), derive(children[0]))
            else -> {
                if (node is F64.Const) {
                    return F64.Const(0.0)
                }
                throw IllegalArgumentException("Don't know how to derive $node")
            }
        })
    }

    @Test
    fun testSimplify() {
        val expr = mul<Unit>(F64.Const(1.0), F64.Const(2.0))

        assertEquals("(f64.mul (f64.const:1.0) (f64.const:2.0))", sExpression(expr))

        val simplified = simplify(expr)

        assertEquals("(f64.const:2.0)", sExpression(simplified))
    }

}
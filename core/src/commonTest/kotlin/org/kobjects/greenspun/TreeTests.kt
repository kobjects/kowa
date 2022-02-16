package org.kobjects.greenspun

import org.kobjects.greenspun.cas.simplify
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Evaluable.Companion.sExpression
import org.kobjects.greenspun.core.F64
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeTests {



    @Test
    fun testSimplify() {
        val expr = F64.mul<Unit>(F64.Const(1.0), F64.Const(2.0))

        assertEquals("(f64.mul (f64.const:1.0) (f64.const:2.0))", sExpression(expr))

        val simplified = simplify(expr)

        assertEquals("(f64.const:2.0)", sExpression(simplified))
    }

}
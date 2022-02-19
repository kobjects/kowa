package org.kobjects.greenspun

import org.kobjects.greenspun.cas.simplify
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.F64
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeTests {



    @Test
    fun testSimplify() {
        val expr = F64.Mul<Unit>(F64.Const(1.0), F64.Const(2.0))

        assertEquals("(1.0 * 2.0)", expr.toString(""))

        val simplified = simplify(expr)

        assertEquals("2.0", simplified.toString(""))
    }

}
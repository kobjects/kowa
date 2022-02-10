package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.Control.*
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Evaluable.Companion.sExpression
import org.kobjects.greenspun.core.F64.Const
import org.kobjects.greenspun.core.F64.add
import org.kobjects.greenspun.core.F64.le
import org.kobjects.greenspun.core.F64.Eq
import org.kobjects.greenspun.core.F64.mod
import org.kobjects.greenspun.core.Node
import org.kobjects.greenspun.core.Str

class ExecutionTests {

    fun String.superTrim(): String {
        val sb = StringBuilder()
        var ignoreWs = true
        for (c in trim()) {
            if (c <= ' ') {
                if (!ignoreWs) {
                    sb.append(' ')
                    ignoreWs = true
                }
            } else {
                sb.append(c)
                ignoreWs = false
            }
        }
        return sb.toString()
    }

    @Test
    fun fizzBuzz() {
        var result = mutableListOf<Any?>()
        var counter = 1.0

        fun Emit(expr: Evaluable<Unit>) = Node("emit", Unit::class, expr) { children, env ->
            result.add(children[0].eval(env))
        }

        fun GetCounter() = Node<Unit>("counter", Double::class) { _, env ->
            counter
        }

        fun SetCounter(expr: Evaluable<Unit>) = Node<Unit>("set_counter", Unit::class) { _, env ->
            counter = expr.evalDouble(env)
            null
        }

        val fizBuzz = While(
            condition = le(GetCounter(), Const(20.0)),
            body = Block (
                If (
                    condition = Eq(mod(GetCounter(), Const(3.0)), Const(0.0)),
                    then = If (
                        condition = Eq(mod(GetCounter(), Const(5.0)), Const(0.0)),
                        then = Emit(Str.Const("Fizz Buzz")),
                        otherwise = Emit(Str.Const("Fizz"))),
                    otherwise = If(
                        condition = Eq(mod(GetCounter(), Const(5.0)), Const(0.0)),
                        then = Emit(Str.Const("Buzz")),
                        otherwise = Emit(GetCounter()))),
                SetCounter(add(GetCounter(), Const(1.0)))))

        assertEquals("""
            (while
              (f64.le (counter) (f64.const:20.0)) 
              (block 
                (if 
                  (f64.eq (f64.mod (counter) (f64.const:3.0)) (f64.const:0.0))
                  (if
                    (f64.eq (f64.mod (counter) (f64.const:5.0)) (f64.const:0.0))
                    (emit (str.const:"Fizz Buzz"))
                    (emit (str.const:"Fizz")))
                  (if
                    (f64.eq (f64.mod (counter) (f64.const:5.0)) (f64.const:0.0))
                    (emit (str.const:"Buzz"))
                    (emit (counter))))
                (set_counter)))""".superTrim(),
            sExpression(fizBuzz))

        fizBuzz.eval(Unit)

        assertEquals(20, result.size)

        assertEquals(mutableListOf(
            1.0, 2.0, "Fizz", 4.0, "Buzz",
            "Fizz", 7.0, 8.0, "Fizz", "Buzz",
            11.0, "Fizz", 13.0, 14.0, "Fizz Buzz",
            16.0, 17.0, "Fizz", 19.0, "Buzz"), result)
    }


}
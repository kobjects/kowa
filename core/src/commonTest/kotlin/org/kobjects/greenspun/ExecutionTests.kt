package org.kobjects.greenspun

import org.kobjects.greenspun.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.Control.*
import org.kobjects.greenspun.core.F64.Const
import org.kobjects.greenspun.core.F64.Add
import org.kobjects.greenspun.core.F64.Le
import org.kobjects.greenspun.core.F64.Eq
import org.kobjects.greenspun.core.F64.Mod

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

        fun Display(expr: Evaluable<Unit>) = Node("display", Void, expr) { children, env ->
            result.add(children[0].eval(env))
        }

        fun GetCounter() = Node<Unit>("counter", F64) { _, env ->
            counter
        }

        fun SetCounter(expr: Evaluable<Unit>) = Node<Unit>("set_counter", Void, expr) { children, context ->
            counter = children[0].evalDouble(context)
            null
        }

        val fizBuzz = While(
            condition = Le(GetCounter(), Const(20.0)),
            body = Block (
                If (
                    condition = Eq(Mod(GetCounter(), Const(3.0)), Const(0.0)),
                    then = If (
                        condition = Eq(Mod(GetCounter(), Const(5.0)), Const(0.0)),
                        then = Display(Str.Const("Fizz Buzz")),
                        otherwise = Display(Str.Const("Fizz"))),
                    otherwise = If(
                        condition = Eq(Mod(GetCounter(), Const(5.0)), Const(0.0)),
                        then = Display(Str.Const("Buzz")),
                        otherwise = Display(GetCounter()))),
                SetCounter(Add(GetCounter(), Const(1.0)))))

        assertEquals("""
            (while (<= (counter) 20.0)
              (begin
                (if (= (% (counter) 3.0) 0.0)
                  (if (= (% (counter) 5.0) 0.0)
                    (display "Fizz Buzz")
                    (display "Fizz"))
                  (if (= (% (counter) 5.0) 0.0)
                    (display "Buzz")
                    (display (counter))))
              (set_counter (+ (counter) 1.0))))
            """.superTrim(),
            fizBuzz.toString().superTrim())

        fizBuzz.eval(Unit)

        assertEquals(20, result.size)

        assertEquals(mutableListOf<Any?>(
            1.0, 2.0, "Fizz", 4.0, "Buzz",
            "Fizz", 7.0, 8.0, "Fizz", "Buzz",
            11.0, "Fizz", 13.0, 14.0, "Fizz Buzz",
            16.0, 17.0, "Fizz", 19.0, "Buzz"), result)
    }


}
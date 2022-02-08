package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.Control.*
import org.kobjects.greenspun.core.Evaluable
import org.kobjects.greenspun.core.Literal
import org.kobjects.greenspun.core.MathOp.Add
import org.kobjects.greenspun.core.RelationalOp.Le
import org.kobjects.greenspun.core.RelationalOp.Eq
import org.kobjects.greenspun.core.MathOp.Mod
import org.kobjects.greenspun.core.Node

class ExecutionTests {

    @Test
    fun fizzBuzz() {
        var result = mutableListOf<Any?>()
        var counter = 1.0

        fun Emit(expr: Evaluable<Unit>) = Node("emit", expr) { children, env ->
            result.add(children[0].eval(env))
        }

        fun GetCounter() = Node<Unit>("counter") { _, env ->
            counter
        }

        fun SetCounter(expr: Evaluable<Unit>) = Node("set_counter", expr) { _, env ->
            counter = expr.evalNumber(env)
            null
        }

        val fizBuzz = While(
            condition = Le(GetCounter(), Literal(20.0)),
            body = Block (
                If (
                    condition = Eq(Mod(GetCounter(), Literal(3.0)), Literal(0.0)),
                    then = If (
                        condition = Eq(Mod(GetCounter(), Literal(5.0)), Literal(0.0)),
                        then = Emit(Literal("Fizz Buzz")),
                        otherwise = Emit(Literal("Fizz"))),
                    otherwise = If(
                        condition = Eq(Mod(GetCounter(), Literal(5.0)), Literal(0.0)),
                        then = Emit(Literal("Buzz")),
                        otherwise = Emit(GetCounter()))),
                SetCounter(Add(GetCounter(), Literal(1.0)))))

        fizBuzz.eval(Unit)

        assertEquals(20, result.size)

        assertEquals(mutableListOf(
            1.0, 2.0, "Fizz", 4.0, "Buzz",
            "Fizz", 7.0, 8.0, "Fizz", "Buzz",
            11.0, "Fizz", 13.0, 14.0, "Fizz Buzz",
            16.0, 17.0, "Fizz", 19.0, "Buzz"), result)
    }


}
package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.Ctrl.*
import org.kobjects.greenspun.Env.*
import org.kobjects.greenspun.Rel.*
import org.kobjects.greenspun.Op.*

class ExecutionTests {

    @Test
    fun fizzBuzz() {
        var result = mutableListOf<Any?>()

        class Emit(val expr: Evaluable) : Evaluable {
            override fun eval(env: Env): Any? {
                result.add(expr.eval(env))
                return null
            }
        }

        val fizBuzz = While(
            condition = Le(GetNumber(0), Literal(20.0)),
            body = Block (
                If (
                    condition = Eq(Mod(GetNumber(0), Literal(3.0)), Literal.ZERO),
                    then = If (
                        condition = Eq(Mod(GetNumber(0), Literal(5.0)), Literal.ZERO),
                        then = Emit(Literal("Fizz Buzz")),
                        otherwise = Emit(Literal("Fizz"))),
                    otherwise = If(
                        condition = Eq(Mod(GetNumber(0), Literal(5.0)), Literal.ZERO),
                        then = Emit(Literal("Buzz")),
                        otherwise = Emit(GetNumber(0)))),
                SetNumber(0, Plus(GetNumber(0), Literal.ONE))))

        fizBuzz.eval(Env(mutableListOf(1.0)))

        assertEquals(20, result.size)

        assertEquals(mutableListOf(
            1.0, 2.0, "Fizz", 4.0, "Buzz",
            "Fizz", 7.0, 8.0, "Fizz", "Buzz",
            11.0, "Fizz", 13.0, 14.0, "Fizz Buzz",
            16.0, 17.0, "Fizz", 19.0, "Buzz"), result)
    }


}
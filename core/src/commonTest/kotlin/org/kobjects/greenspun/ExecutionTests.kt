package org.kobjects.greenspun

import org.kobjects.greenspun.core.context.LocalRuntimeContext
import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.control.If
import org.kobjects.greenspun.core.control.While
import org.kobjects.greenspun.core.data.Void
import org.kobjects.greenspun.core.dsl.Func
import org.kobjects.greenspun.core.tree.LambdaNode
import org.kobjects.greenspun.core.tree.Node

class ExecutionTests {

    @Test
    fun fizzBuzz() {
        var result = mutableListOf<Any>()

        fun Log(expr: Any) = LambdaNode("Log", Void, Node.of(expr)) { context, children ->
            result.add(children[0].eval(context))
        }

        val fizBuzz =
            Func(Void) {
                val count = Var(1.0)
                +While(count Le 20.0,
                    Block {
                        +If (count % 3.0 Eq 0.0,
                            If (count % 5.0 Eq 0.0,
                                Log("Fizz Buzz"),
                                Log("Fizz")),
                            If(count % 5.0 Eq 0.0,
                                Log("Buzz"),
                                Log(count))
                        )
                        +Set(count, count + 1.0)
                    }
                )
            }


        fizBuzz.func(LocalRuntimeContext())

                assertEquals("""
                    Block { 
                      +val local0 = Var(F64(1.0))
                      +While((local0 LE F64(20.0)),
                        Block { 
                          +If(((local0 % F64(3.0)) EQ F64(0.0)),
                            If(((local0 % F64(5.0)) EQ F64(0.0)),
                              Log(Str("Fizz Buzz")),
                              Log(Str("Fizz"))),
                            If(((local0 % F64(5.0)) EQ F64(0.0)), 
                              Log(Str("Buzz")),
                            Log(local0)))
                          +Set(local0, (local0 + F64(1.0)))
                        }
                    }
                    """.superTrim(),
                    fizBuzz.func.body.toString().superTrim())


        assertEquals(20, result.size)

        assertEquals(mutableListOf<Any>(
            1.0, 2.0, "Fizz", 4.0, "Buzz",
            "Fizz", 7.0, 8.0, "Fizz", "Buzz",
            11.0, "Fizz", 13.0, 14.0, "Fizz Buzz",
            16.0, 17.0, "Fizz", 19.0, "Buzz"), result)
    }


    companion object {

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

    }
}
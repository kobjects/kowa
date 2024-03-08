package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.control.If
import org.kobjects.greenspun.core.control.While
import org.kobjects.greenspun.core.data.F64
import org.kobjects.greenspun.core.data.Str
import org.kobjects.greenspun.core.data.Void
import org.kobjects.greenspun.core.dsl.Module

class ExecutionTests {

    val fizzBuzzModule = Module {
        val LogStr = ImportFunc("logStr", Void, Str)
        val LogF64 = ImportFunc("logF64", Void, F64)

        val fizzBuzz = Func(Void) {
            val count = Local(1.0)
            +While(count Le 20.0,
                Block {
                    +If(
                        count % 3.0 Eq 0.0,
                        If(
                            count % 5.0 Eq 0.0,
                            LogStr("Fizz Buzz"),
                            LogStr("Fizz")
                        ),
                        If(
                            count % 5.0 Eq 0.0,
                            LogStr("Buzz"),
                            LogF64(count)
                        )
                    )
                    +Set(count, count + 1.0)
                }
            )
        }

        Export("fizzBuzz", fizzBuzz)
    }


    @Test
    fun fizzBuzz() {
        var result = mutableListOf<Any>()

        val fizzBuzzInstance = fizzBuzzModule.createInstance(
            "logStr" to { result.add(it[0]) },
            "logF64" to { result.add(it[0]) }
        )

        fizzBuzzInstance.exports["fizzBuzz"]!!()

        assertEquals(20, result.size)

        assertEquals(mutableListOf<Any>(
            1.0, 2.0, "Fizz", 4.0, "Buzz",
            "Fizz", 7.0, 8.0, "Fizz", "Buzz",
            11.0, "Fizz", 13.0, 14.0, "Fizz Buzz",
            16.0, 17.0, "Fizz", 19.0, "Buzz"), result)

                assertEquals("""
                    Block { 
                      val local0 = Local(F64(1.0))
                      +While((local0 Le F64(20.0)),
                        Block { 
                          +If(((local0 % F64(3.0)) Eq F64(0.0)),
                            If(((local0 % F64(5.0)) Eq F64(0.0)),
                              import0(Str("Fizz Buzz")),
                              import0(Str("Fizz"))),
                            If(((local0 % F64(5.0)) Eq F64(0.0)), 
                              import0(Str("Buzz")),
                            import1(local0)))
                          +Set(local0, (local0 + F64(1.0)))
                        }
                    }
                    """.superTrim(),
                    fizzBuzzModule.funcs[0].body.toString().superTrim())

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
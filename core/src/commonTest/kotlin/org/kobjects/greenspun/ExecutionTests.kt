package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.control.If
import org.kobjects.greenspun.core.control.While
import org.kobjects.greenspun.core.types.I64
import org.kobjects.greenspun.core.types.Str
import org.kobjects.greenspun.core.types.Void
import org.kobjects.greenspun.core.module.Module

class ExecutionTests {

    val fizzBuzzModule = Module {
        val LogStr = ImportFunc("logStr", Void, Str)
        val LogI64 = ImportFunc("logI64", Void, I64)

        val fizzBuzz = Func(Void) {
            val count = Local(1L)
            +While(count Le 20L,
                Block {
                    +If(count % 3L Eq 0L,
                        If(count % 5L Eq 0L,
                            LogStr("Fizz Buzz"),
                            LogStr("Fizz")),
                        If(count % 5L Eq 0L,
                            LogStr("Buzz"),
                            LogI64(count)))
                    +Set(count, count + 1L)
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
            "logI64" to { result.add(it[0]) }
        )

        fizzBuzzInstance.exports["fizzBuzz"]!!()

        assertEquals(20, result.size)

        assertEquals(mutableListOf<Any>(
            1L, 2L, "Fizz", 4L, "Buzz",
            "Fizz", 7L, 8L, "Fizz", "Buzz",
            11L, "Fizz", 13L, 14L, "Fizz Buzz",
            16L, 17L, "Fizz", 19L, "Buzz"), result)

                assertEquals("""
                    Block { 
                      val local0 = Local(I64(1))
                      +While((local0 Le I64(20)),
                        Block { 
                          +If(((local0 % I64(3)) Eq I64(0)),
                            If(((local0 % I64(5)) Eq I64(0)),
                              import0(Str("Fizz Buzz")),
                              import0(Str("Fizz"))),
                            If(((local0 % I64(5)) Eq I64(0)), 
                              import0(Str("Buzz")),
                            import1(local0)))
                          +Set(local0, (local0 + I64(1)))
                        })
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
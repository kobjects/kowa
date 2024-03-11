package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.control.If
import org.kobjects.greenspun.core.control.While
import org.kobjects.greenspun.core.module.ImportObject
import org.kobjects.greenspun.core.type.I64
import org.kobjects.greenspun.core.type.Str
import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32

class ExecutionTests {

    val fizzBuzzModule = Module {
        val LogStr = ImportFunc("test", "logStr", Void, Str)
        val LogI32 = ImportFunc("test", "logI32", Void, I32)

        val fizzBuzz = Func(Void) {
            val count = Local(1)
            +While(count Le 20,
                Block {
                    +If(count % 3 Eq 0,
                        If(count % 5 Eq 0,
                            LogStr("Fizz Buzz"),
                            LogStr("Fizz")),
                        If(count % 5 Eq 0,
                            LogStr("Buzz"),
                            LogI32(count)))
                    +Set(count, count + 1)
                }
            )
        }

        Export("fizzBuzz", fizzBuzz)
    }


    @Test
    fun fizzBuzz() {
        var result = mutableListOf<Any>()

        val importObject = ImportObject()
        importObject.addFunc("test", "logStr") { result.add(it[0]) }
        importObject.addFunc("test", "logI32") { result.add(it[0]) }

        val fizzBuzzInstance = fizzBuzzModule.createInstance(importObject)

        fizzBuzzInstance.exports["fizzBuzz"]!!()

        assertEquals(20, result.size)

        assertEquals(mutableListOf<Any>(
            1, 2, "Fizz", 4, "Buzz",
            "Fizz", 7, 8, "Fizz", "Buzz",
            11, "Fizz", 13, 14, "Fizz Buzz",
            16, 17, "Fizz", 19, "Buzz"), result)

                assertEquals("""
                    Block { 
                      val local0 = Local(I32(1))
                      +While((local0 Le I32(20)),
                        Block { 
                          +If(((local0 % I32(3)) Eq I32(0)),
                            If(((local0 % I32(5)) Eq I32(0)),
                              import0(Str("Fizz Buzz")),
                              import0(Str("Fizz"))),
                            If(((local0 % I32(5)) Eq I32(0)), 
                              import0(Str("Buzz")),
                            import1(local0)))
                          +Set(local0, (local0 + I32(1)))
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
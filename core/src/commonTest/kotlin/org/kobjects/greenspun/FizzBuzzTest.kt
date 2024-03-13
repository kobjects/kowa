package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.control.If
import org.kobjects.greenspun.core.control.While
import org.kobjects.greenspun.core.module.ImportObject
import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32

class FizzBuzzTest {

    val fizzBuzzModule = Module {

        val fizz = ActiveData("Fizz")
        val buzz = ActiveData("Buzz")
        val fizzBuzz = ActiveData("FizzBuzz")

        val LogStr = ImportFunc("console", "logStr", Void, I32, I32)
        val LogI32 = ImportFunc("console", "logI32", Void, I32)

        ExportFunc("fizzBuzz", Void) {
            val count = Local(1)
            +While(count Le 20,
                Block {
                    +If(count % 3 Eq 0,
                        If(count % 5 Eq 0,
                            LogStr(fizzBuzz, 8),
                            LogStr(fizz, 4)),
                        If(count % 5 Eq 0,
                            LogStr(buzz, 4),
                            LogI32(count)))
                    +Set(count, count + 1)
                }
            )
        }
    }



    @Test
    fun fizzBuzz() {

        var result = mutableListOf<Any>()

        val importObject = ImportObject()

        importObject.addFunc("console", "logI32") { _, params -> result.add(params[0]) }
        importObject.addFunc("console", "logStr") { instance, params ->
            val memPos = params[0] as Int
            val size = params[1] as Int
            val bytes = instance.memory.copyOfRange(memPos, memPos + size)
            result.add(bytes.decodeToString())
        }

        val fizzBuzzInstance = fizzBuzzModule.createInstance(importObject)

        fizzBuzzInstance.funcExports["fizzBuzz"]!!()

        assertEquals(20, result.size)

        assertEquals(mutableListOf<Any>(
            1, 2, "Fizz", 4, "Buzz",
            "Fizz", 7, 8, "Fizz", "Buzz",
            11, "Fizz", 13, 14, "FizzBuzz",
            16, 17, "Fizz", 19, "Buzz"), result)

                assertEquals("""
                    Block { 
                      val local0 = Local(I32(1))
                      +While((local0 Le I32(20)),
                        Block { 
                          +If(((local0 % I32(3)) Eq I32(0)),
                            If(((local0 % I32(5)) Eq I32(0)),
                              import0(I32(8), I32(8)),
                              import0(I32(0), I32(4))),
                            If(((local0 % I32(5)) Eq I32(0)), 
                              import0(I32(4), I32(4)),
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
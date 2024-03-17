package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.control.If
import org.kobjects.greenspun.core.control.While
import org.kobjects.greenspun.core.instance.ImportObject
import org.kobjects.greenspun.core.instance.Memory
import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32

class FizzBuzzTest {

    val fizzBuzzModule = Module {

        ImportMemory("console", "memory", 1)

        val fizz = Data("Fizz")
        val buzz = Data("Buzz")
        val fizzBuzz = Data("FizzBuzz")

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
    fun fizzBuzzTest() {

        var result = mutableListOf<Any>()
        val memory = Memory(1)

        val importObject = ImportObject()
        importObject.addMemory("console", "memory", memory)
        importObject.addFunc("console", "logI32") { params -> result.add(params[0]) }
        importObject.addFunc("console", "logStr") { params ->
            val memPos = params[0] as Int
            val size = params[1] as Int
            val bytes = memory.buffer.copyOfRange(memPos, memPos + size)
            result.add(bytes.decodeToString())
        }

        val fizzBuzzInstance = fizzBuzzModule.instantiate(importObject)

        fizzBuzzInstance.funcExports["fizzBuzz"]!!()

        assertEquals(20, result.size)

        assertEquals(mutableListOf<Any>(
            1, 2, "Fizz", 4, "Buzz",
            "Fizz", 7, 8, "Fizz", "Buzz",
            11, "Fizz", 13, 14, "FizzBuzz",
            16, 17, "Fizz", 19, "Buzz"), result)



        assertEquals("""
            Module {
              ImportMemory("console", "memory", 1)
            
              val func0 = ImportFunc("console", "logStr", Void, I32, I32)
              val func1 = ImportFunc("console", "logI32", Void, I32)
      
              val func2 = Func(Void) {
                val local0 = Local(I32(1))
                +While((local0 Le I32(20)),
                  Block {
                    +If(((local0 % I32(3)) Eq I32(0)),
                      If(((local0 % I32(5)) Eq I32(0)),
                        func0(I32(8), I32(8)),
                        func0(I32(0), I32(4))),
                      If(((local0 % I32(5)) Eq I32(0)),
                        func0(I32(4), I32(4)),
                        func1(local0)))
                    +Set(local0, (local0 + I32(1)))
                  })
              }
              Export("fizzBuzz", func2) 
            }
            """.superTrim(),
            fizzBuzzModule.toString().superTrim())
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
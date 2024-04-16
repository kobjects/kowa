package org.kobjects.greenspun

import kotlin.test.Test
import kotlin.test.assertEquals
import org.kobjects.greenspun.core.runtime.ImportObject
import org.kobjects.greenspun.core.runtime.Memory
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32

class FizzBuzzTest {

    val fizzBuzzModule = Module {

        val memory = ImportMemory("console", "memory", 1)

        val fizz = memory.data("Fizz")
        val buzz = memory.data("Buzz")
        val fizzBuzz = memory.data("FizzBuzz")

        val LogStr = ImportFunc("console", "logStr") { Param(I32, I32) }
        val LogI32 = ImportFunc("console", "logI32") { Param(I32) }

        val fizzBuzzFunc = Func {
            For(1, 21) { count ->
                If(count % 3 Eq 0) {
                    If(count % 5 Eq 0) {
                        LogStr(fizzBuzz, fizzBuzz.len)
                    }.Else {
                        LogStr(fizz, fizz.len)
                    }
                }.Else {
                    If(count % 5 Eq 0) {
                        LogStr(buzz, buzz.len)
                    }.Else {
                        LogI32(count)
                    }
                }
            }
        }

        Export("fizzBuzz", fizzBuzzFunc)
    }



    @Test
    fun functionalTest() {

        var result = mutableListOf<Any>()
        val memory = Memory(1)

        val importObject = ImportObject()
        importObject.addMemory("console", "memory", memory)
        importObject.addFunc("console", "logI32") { params -> result.add(params[0]) }
        importObject.addFunc("console", "logStr") { params ->
            val memPos = params[0] as Int
            val size = params[1] as Int
            val bytes = memory.bytes.copyOfRange(memPos, memPos + size)
            result.add(bytes.decodeToString())
        }

        val fizzBuzzInstance = fizzBuzzModule.instantiate(importObject)

        fizzBuzzInstance.funcExports["fizzBuzz"]!!()

        assertEquals(20, result.size)

        assertEquals(
            mutableListOf<Any>(
                1, 2, "Fizz", 4, "Buzz",
                "Fizz", 7, 8, "Fizz", "Buzz",
                11, "Fizz", 13, 14, "FizzBuzz",
                16, 17, "Fizz", 19, "Buzz"
            ), result
        )

    }

}
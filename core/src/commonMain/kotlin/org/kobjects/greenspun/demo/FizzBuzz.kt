package org.kobjects.greenspun.demo


import org.kobjects.greenspun.core.runtime.ImportObject
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32

fun main(argv: Array<String>) {

    val fizzBuzzModule = Module {

        val memory = Memory(1)

        val fizz = memory.data("Fizz")
        val buzz = memory.data("Buzz")

        val LogStr = ImportFunc("console", "logStr") { Param(I32, I32) }
        val LogI32 = ImportFunc("console", "logI32") { Param(I32) }

        val fizzBuzzFunc = Func {
            For(1, 21) { count ->
                If(count % 3 Eq 0) {
                    If(count % 5 Eq 0) {
                        LogStr(fizz, fizz.len + buzz.len)
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

    val importObject = ImportObject()
    importObject.addFunc("console", "logI32") { _, params -> println(params[0]) }
    importObject.addFunc("console", "logStr") { instance, params ->
        val memPos = params[0] as Int
        val size = params[1] as Int
        val bytes = instance.memory.bytes.copyOfRange(memPos, memPos + size)
        println(bytes.decodeToString())
    }

    val fizzBuzzInstance = fizzBuzzModule.instantiate(importObject)

    fizzBuzzInstance.funcExports["fizzBuzz"]!!()

}


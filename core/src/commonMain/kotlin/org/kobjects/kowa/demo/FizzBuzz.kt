package org.kobjects.kowa.demo


import org.kobjects.kowa.runtime.ImportObject
import org.kobjects.kowa.core.module.Module
import org.kobjects.kowa.core.type.Bool
import org.kobjects.kowa.core.type.I32
import org.kobjects.kowa.wasi.addStdIoImpl

@OptIn(ExperimentalStdlibApi::class)
fun main(argv: Array<String>) {

    val fizzBuzzModule = Module {

        val memory = Export("memory", Memory(1))

        // Helpers for printing strings and numbers via fd_write

        // Reserve 16 bytes space for fd_write data
        val digit = memory.data(16, "0")

        val fd_write = ImportFunc("wasi_snapshot_preview1", "fd_write", I32) { Param(I32, I32, I32, I32) }

        val Print = Func {
            val address = Param(I32)
            val len = Param(I32)

            memory.i32[0] = address
            memory.i32[4] = len
            Drop(fd_write(1, 0, 1, 8))
        }

        val PrintI32Inner = ForwardDecl() { Param(I32, Bool) }

        Implementation(PrintI32Inner) {
            val i = Param(I32)
            val uncoditional = Param(Bool)

            If ((i Gt 0) Or uncoditional) {
                PrintI32Inner(i / 10, false)
                memory.u8i32[digit] = i % 10 + 48
                Print(digit, 1)
            }
        }

        val PrintI32 = Func {
            val i = Param(I32)
            PrintI32Inner(i, true)
        }

        // "Actual" FizzBuzz

        val fizz = memory.data("Fizz")
        val buzz = memory.data("Buzz")
        val newline = memory.data("\n")

        val fizzBuzzFunc = Func {
            For(1, 21) { count ->
                If(count % 3 Eq 0) {
                    Print(fizz, fizz.len)
                    If(count % 5 Eq 0) {
                        Print(buzz, buzz.len)
                    }
                }.Else {
                    If(count % 5 Eq 0) {
                        Print(buzz, buzz.len)
                    }.Else {
                        PrintI32(count)
                    }
                }
                Print(newline, 1)
            }
        }

        Start(fizzBuzzFunc)
    }

    println("Instantiating module:\n")

    val importObject = ImportObject()
    importObject.addStdIoImpl()
    fizzBuzzModule.instantiate(importObject)

    println("\n\nFizzBuzz wasm binary code:\n")
    println(fizzBuzzModule.toWasm().toHexString(HexFormat {
        bytes.bytesPerLine = 32
        bytes.bytesPerGroup = 16
        bytes.groupSeparator = "  "
    }))
}


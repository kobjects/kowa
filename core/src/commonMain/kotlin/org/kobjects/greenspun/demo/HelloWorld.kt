package org.kobjects.greenspun.demo


import org.kobjects.greenspun.runtime.ImportObject
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.wasi.addStdIoImpl

@OptIn(ExperimentalStdlibApi::class)
fun main(argv: Array<String>) {

    // The Hello World

    val module = Module {

        val memory = Memory(1)

        val helloWorld = memory.data("Hello World\n")
        val write_result = memory.data("1234")

        val fd_write = ImportFunc("wasi_snapshot_preview1", "fd_write", I32) { Param(I32, I32, I32, I32) }

        val PrintHelloWorld = Func {
            Drop(fd_write(1, helloWorld, helloWorld.len, write_result))
        }

        Start(PrintHelloWorld)
    }

    println("Instantiating module:\n")

    val importObject = ImportObject()
    importObject.addStdIoImpl()
    module.instantiate(importObject)

    println("\n\nHelloWorld wasm binary code:\n")
    println(module.toWasm().toHexString(HexFormat {
        bytes.bytesPerLine = 32
        bytes.bytesPerGroup = 16
        bytes.groupSeparator = "  "
    }))


}


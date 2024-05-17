package org.kobjects.kowa.demo


import org.kobjects.kowa.runtime.ImportObject
import org.kobjects.kowa.core.module.Module
import org.kobjects.kowa.core.type.I32
import org.kobjects.kowa.wasi.addStdIoImpl

@OptIn(ExperimentalStdlibApi::class)
fun main(argv: Array<String>) {

    // The Hello World

    val module = Module {

        val memory = Export("memory", Memory(1))

        // Leave some space for fd_write data
        val helloWorld = memory.data(16, "Hello World\n")

        val fd_write = ImportFunc("wasi_snapshot_preview1", "fd_write", I32) { Param(I32, I32, I32, I32) }

        val hello = Func {
            memory.i32[0] = helloWorld
            memory.i32[4] = helloWorld.len
            Drop(fd_write(1, 0, 1, 8))
        }

        Start(hello)
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


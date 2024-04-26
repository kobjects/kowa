package org.kobjects.greenspun.wasi

import org.kobjects.greenspun.binary.loadI32
import org.kobjects.greenspun.binary.storeI32
import org.kobjects.greenspun.runtime.ImportObject

fun ImportObject.addStdIoImpl() {

    addFunc("wasi_snapshot_preview1", "fd_write") { instance, params ->
        val fd = params[0] as Int
        require(fd == 1) {
            "Currently, only fd 1 (stdout) is supported for fd_write."
        }
        val ioVector = params[1] as Int
        val count = params[2] as Int
        var nwritten = 0
        for (i in 0 until count) {
            val address = instance.memory.bytes.loadI32(ioVector + i * 8)
            val len = instance.memory.bytes.loadI32(ioVector + i * 8 + 4)
            val bytes = instance.memory.bytes.copyOfRange(address, address + len)
            print(bytes.decodeToString())
            nwritten += bytes.size
        }
        instance.memory.bytes.storeI32(params[3] as Int, nwritten)
        0
    }

}
package org.kobjects.greenspun.wasi

import org.kobjects.greenspun.core.binary.storeI32
import org.kobjects.greenspun.core.runtime.ImportObject

fun ImportObject.addStdIoImpl() {

    addFunc("wasi_snapshot_preview1", "fd_write") { instance, params ->
        val fd = params[0] as Int
        require(fd == 1) {
            "Currently, only fd 1 (stdout) is supported for fd_write."
        }
        val memPos = params[1] as Int
        val len = params[2] as Int
        val nwritten = params[3] as Int
        val bytes = instance.memory.bytes.copyOfRange(memPos, memPos + len)
        instance.memory.bytes.storeI32(nwritten, len)
        print(bytes.decodeToString())
        len
    }

}
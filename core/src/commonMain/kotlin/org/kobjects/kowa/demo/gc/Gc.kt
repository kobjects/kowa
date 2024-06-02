package org.kobjects.kowa.demo.gc

import org.kobjects.kowa.core.gc.Arr
import org.kobjects.kowa.core.gc.Ref
import org.kobjects.kowa.core.gc.StructImpl
import org.kobjects.kowa.core.module.Module
import org.kobjects.kowa.core.type.I32
import org.kobjects.kowa.core.type.I64

@OptIn(ExperimentalStdlibApi::class)
fun main(argv: Array<String>) {


    val module = Module {

        val buf = object : StructImpl() {
            val pos = Field(I64)
            val data = Field(Ref(Arr(I32)))
        }
    }
}


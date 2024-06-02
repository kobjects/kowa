package org.kobjects.kowa.core.gc

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.type.Type

open class StructImpl : Type {
    val fields = mutableListOf<Field>()




    inner class Field(val type: Type) {
        init {
            fields.add(this)
        }
    }

    override fun toWasm(writer: WasmWriter) {
        TODO("Not yet implemented")
    }
}
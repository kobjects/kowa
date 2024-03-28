package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type

interface Idx {
    val index: Int
    val idx: IdxNode
        get() = IdxNode(index)


    class IdxNode(val index: Int) : AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = index

        override fun toString(writer: CodeWriter) {
            writer.write("IdxNode($index)")
        }

        override fun toWasm(writer: ModuleWriter) {
            writer.write(WasmOpcode.I32_CONST)
            writer.writeI32(index)
        }

        override val returnType = I32
    }
}
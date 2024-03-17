package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

class Nop() : AbstractLeafNode() {
    override fun eval(context: LocalRuntimeContext) = Unit

    override fun toString(writer: CodeWriter) = writer.write("Nop()")

    override fun toWasm(writer: ModuleWriter) {
        writer.write(WasmOpcode.NOP)
    }

    override val returnType: Type
        get() = Void
}
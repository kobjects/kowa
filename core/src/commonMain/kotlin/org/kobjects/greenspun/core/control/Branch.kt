package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.tree.AbstractLeafNode
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

class Branch(val label: Int = 0) : AbstractLeafNode() {
    override fun eval(context: LocalRuntimeContext) = throw BranchSignal(label)

    override fun toString(writer: CodeWriter) {
        writer.write("Branch(")
        if (label != 0) {
            writer.write(label)
        }
        writer.write(")")
    }

    override fun toWasm(writer: ModuleWriter) {
        writer.write(WasmOpcode.BR)
        writer.writeU32(label)
    }

    override val returnType: Type
        get() = Void
}
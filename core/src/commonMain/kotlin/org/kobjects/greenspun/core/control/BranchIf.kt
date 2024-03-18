package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

class BranchIf(val condition: Node, val label: Int = 0) : Node() {
    override fun eval(context: LocalRuntimeContext): Any =
        if (condition.evalBool(context)) FlowSignal(label) else Unit

    override fun children() = listOf(condition)

    override fun reconstruct(newChildren: List<Node>) = BranchIf(newChildren[0], label)

    override fun toString(writer: CodeWriter) {
        writer.write("BranchIf(")
        condition.toString(writer)
        if (label != 0) {
            writer.write(", $label")
        }
        writer.write(")")
    }

    override fun toWasm(writer: ModuleWriter) {
        writer.write(WasmOpcode.BR_IF)
        writer.writeU32(label)
    }

    override val returnType: Type
        get() = Void
}
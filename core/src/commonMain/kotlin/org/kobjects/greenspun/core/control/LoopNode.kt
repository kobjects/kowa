package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.control.Sequence.Companion.stringifyAsSequence
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

class LoopNode(val child: Node) : Node() {

    init {
        require(child.returnType == Void) {
            "Void type expected for Loop body."
        }
    }
    override fun eval(context: LocalRuntimeContext) {
        while(true) {
            try {
                child.eval(context)
            } catch (signal: BranchSignal) {
                if (signal.label > 0) {
                    throw BranchSignal(signal.label - 1)
                }
                continue
            }
        }
    }

    override fun children() = listOf(child)

    override fun reconstruct(newChildren: List<Node>) = LoopNode(newChildren[0] as Sequence)

    override fun toString(writer: CodeWriter) {
        writer.write("Loop {")
        val inner = writer.indented()
        inner.newLine()
        child.stringifyAsSequence(inner)
        writer.newLine()
        writer.write("}")
    }

    override fun toWasm(writer: ModuleWriter) {
        writer.write(WasmOpcode.LOOP)
        child.toWasm(writer)
        writer.write(WasmOpcode.END)
    }

    override val returnType: Type
        get() = Void
}
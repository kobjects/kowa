package org.kobjects.greenspun.core.tree

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

class Drop(val child: Node) : Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        child.eval(context)
        return Unit
    }

    override fun children(): List<Node> = listOf(child)

    override fun reconstruct(newChildren: List<Node>) = Drop(newChildren[0])

    override fun toString(writer: CodeWriter) {
        writer.write("Unused(")
        child.toString(writer)
        writer.write(")")
    }

    override fun toWasm(writer: WasmWriter) {
        if (child.returnType != Void) {
            writer.write(WasmOpcode.DROP)
        }
    }

    override val returnType: Type
        get() = Void
}
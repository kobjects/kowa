package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.expression.CodeWriter
import org.kobjects.greenspun.core.expression.Node
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmWriter

class LocalReference(
    val index: Int,
    val mutable: Boolean,
    override val returnType: Type
) : Node() {
    override fun eval(context: LocalRuntimeContext) = context.getLocal(index)

    override fun children(): List<Node> = emptyList()

    override fun reconstruct(newChildren: List<Node>) = this

    override fun toString(writer: CodeWriter) =
        writer.write("local$index")

    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmOpcode.LOCAL_GET)
        writer.writeU32(index)
    }
}
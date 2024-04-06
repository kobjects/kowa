package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expression.AbstractLeafNode
import org.kobjects.greenspun.core.expression.CodeWriter
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type


class MemorySize() : AbstractLeafNode() {
    override fun eval(context: LocalRuntimeContext) =
        context.instance.memory.buffer.size / 65536

    override fun toString(writer: CodeWriter) = writer.write("MemorySize()")

    override fun toWasm(writer: WasmWriter) = writer.write(WasmOpcode.MEMORY_SIZE)

    override val returnType: Type
        get() = I32
}
package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type


class MemorySize() : Expr() {


    override fun toString(writer: CodeWriter) = writer.write("MemorySize()")

    override fun toWasm(writer: WasmWriter) = writer.write(WasmOpcode.MEMORY_SIZE)

    override val returnType: List<Type>
        get() = listOf(I32)
}
package org.kobjects.kowa.core.memory

import org.kobjects.kowa.binary.WasmOpcode
import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.expr.Expr
import org.kobjects.kowa.core.type.I32
import org.kobjects.kowa.core.type.Type

class MemoryView(
    val memory: MemoryInterface,
    val name: String,
    val type: Type,
    val loadOpcode: WasmOpcode,
    val storeOpcode: WasmOpcode,
    val defaultAlign: Int = 0,
    val baseOffset: Int = 0
) {

    operator fun get(address: Any, align: Int = defaultAlign, offset: Int = 0) =
        Load(name, type, loadOpcode, address, align, baseOffset + offset)


    class Load(
        val name: String,
        val type: Type,
        val opcode: WasmOpcode,
        address: Any,
        val align: Int,
        val offset: Int
    ): Expr(address) {

        init {
            require(parameterTypes() == listOf(I32))
        }
        override fun toString(writer: CodeWriter) {
            writer.write(name, "(")
            stringifyChildren(writer, "", ", ", "")
            writer.write(", ", align, offset, ")")
        }

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(opcode)
            writer.writeU32(align)
            writer.writeU32(offset)
        }

        override val returnType: List<Type>
            get() = listOf(type)

    }

}
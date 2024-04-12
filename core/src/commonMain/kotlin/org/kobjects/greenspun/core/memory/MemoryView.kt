package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.WasmType

class MemoryView(
    val memory: MemoryInterface,
    val name: String,
    val type: WasmType,
    val loadOpcode: WasmOpcode,
    val storeOpcode: WasmOpcode,
    val defaultAlign: Int = 0,
    val baseOffset: Int = 0
) {

    operator fun get(address: Any, align: Int = defaultAlign, offset: Int = 0) =
        Load(name, type, loadOpcode, address, align, baseOffset + offset)


    class Load(
        val name: String,
        val type: WasmType,
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
            writer.write(opcode)
            writer.writeU32(align)
            writer.writeU32(offset)
        }

        override val returnType: List<WasmType>
            get() = listOf(type)

    }

}
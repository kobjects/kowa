package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.*

interface MemoryInterface : Exportable {
    val min: Int
    val max: Int?

    override fun writeExportDescription(writer: WasmWriter) {
        writer.writeByte(2)
        writeType(writer)
    }

    override fun writeExportDescription(writer: CodeWriter) {
        writer.write("Export(memory)")
    }

    fun writeType(writer: WasmWriter) {
        val max = max
        if (max == null) {
            writer.writeByte(0)
            writer.writeU32(min)
        } else {
            writer.writeByte(1)
            writer.writeU32(min)
            writer.writeU32(max)
        }
    }


    val size: Expr
        get() = object : Expr() {


        override fun toString(writer: CodeWriter) = writer.write("MemorySize()")

        override fun toWasm(writer: WasmWriter) = writer.write(WasmOpcode.MEMORY_SIZE)

        override val returnType: List<Type>
            get() = listOf(I32)
    }


    fun load(type: Type, address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("load", type, mapOf(
            I32 to WasmOpcode.I32_LOAD,
            I64 to WasmOpcode.I64_LOAD,
            F32 to WasmOpcode.F32_LOAD,
            F64 to WasmOpcode.F64_LOAD
            ), address, align, offset)

    fun load8U(type: Type, address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("load8U", I32, mapOf(
            I32 to WasmOpcode.I32_LOAD_8_U,
            I64 to WasmOpcode.I64_LOAD_8_U
        ), address, align, offset)

    fun load8S(type: Type, address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("load8S", I32, mapOf(
            I32 to WasmOpcode.I32_LOAD_8_S,
            I64 to WasmOpcode.I64_LOAD_8_S
        ), address, align, offset)

    fun load16U(type: Type, address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("load16U", I32, mapOf(
            I32 to WasmOpcode.I32_LOAD_16_U,
            I64 to WasmOpcode.I64_LOAD_16_U
        ), address, align, offset)

    fun load16S(type: Type, address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("load16S", I32, mapOf(
            I32 to WasmOpcode.I32_LOAD_16_U,
            I64 to WasmOpcode.I64_LOAD_16_U
        ), address, align, offset)

    fun load32U(type: Type, address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("load32U", I32, mapOf(
            I64 to WasmOpcode.I64_LOAD_16_U
        ), address, align, offset)

    fun load32S(type: Type, address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("load32S", I32, mapOf(
            I64 to WasmOpcode.I64_LOAD_16_U
        ), address, align, offset)


    private class Load(
        val name: String,
        val type: Type,
        opcodes: Map<Type, WasmOpcode>,
        address: Any,
        val align: Int,
        val offset: Int
    ): Expr(address) {

        val opcode = opcodes[type] ?: throw UnsupportedOperationException("$name not defined for $type")

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

        override val returnType: List<Type>
            get() = listOf(type)

    }

}
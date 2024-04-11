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

    operator fun get(address: Any, align: Int = 0, offset: Int = 0) = MemoryAccessBase(address, align, offset)

    fun load(address: Any, align: Int = 0, offset: Int = 0) = MemoryAccessBase(address, align, offset)

    class MemoryAccessBase(val address: Any, val align: Int, val offset: Int) {

        val i32: Expr
            get() = Load("i32", I32, WasmOpcode.I32_LOAD, address, align, offset)

        val i64: Expr
            get() = Load("i64", I64, WasmOpcode.I64_LOAD, address, align, offset)

        val f32: Expr
            get() = Load("f32", F32, WasmOpcode.F32_LOAD, address, align, offset)

        val f64: Expr
            get() = Load("f64", F64, WasmOpcode.F64_LOAD, address, align, offset)

    }


    fun load8S(address: Any, align: Int = 0, offset: Int = 0) =
        Memory8SAccessBase(address, align, offset)

    class Memory8SAccessBase(val address: Any, val align: Int, val offset: Int) {
        val i32: Expr
            get() = Load("s8i32", I32, WasmOpcode.I32_LOAD_8_S, address, align, offset)

        val i64: Expr
            get() = Load("s8i64", I64, WasmOpcode.I64_LOAD_8_S, address, align, offset)
    }

    fun load8U(address: Any, align: Int = 0, offset: Int = 0) =
        Memory8UAccessBase(address, align, offset)

    class Memory8UAccessBase(val address: Any, val align: Int, val offset: Int) {
        val i32: Expr
            get() = Load("u8i32", I32, WasmOpcode.I32_LOAD_8_U, address, align, offset)

        val i64: Expr
            get() = Load("u8i64", I64, WasmOpcode.I64_LOAD_8_U, address, align, offset)
    }

    fun load16S(address: Any, align: Int = 0, offset: Int = 0) =
        Memory16SAccessBase(address, align, offset)

    class Memory16SAccessBase(val address: Any, val align: Int, val offset: Int) {
        val i32: Expr
            get() = Load("s16i32", I32, WasmOpcode.I32_LOAD_16_S, address, align, offset)

        val i64: Expr
            get() = Load("s16i64", I64, WasmOpcode.I64_LOAD_16_S, address, align, offset)
    }

    fun load16U(address: Any, align: Int = 0, offset: Int = 0) =
        Memory16UAccessBase(address, align, offset)

    class Memory16UAccessBase(val address: Any, val align: Int, val offset: Int) {
        val i32: Expr
            get() = Load("u16i32", I32, WasmOpcode.I32_LOAD_16_U, address, align, offset)

        val i64: Expr
            get() = Load("u16i64", I64, WasmOpcode.I64_LOAD_16_U, address, align, offset)
    }



    fun load32S(address: Any, align: Int = 0, offset: Int = 0) =
        Memory32SAccessBase(address, align, offset)

    class Memory32SAccessBase(val address: Any, val align: Int, val offset: Int) {

        val i64: Expr
            get() = Load("s32i64", I64, WasmOpcode.I64_LOAD_32_S, address, align, offset)
    }

    fun load32U(address: Any, align: Int = 0, offset: Int = 0) =
        Memory32UAccessBase(address, align, offset)

    class Memory32UAccessBase(val address: Any, val align: Int, val offset: Int) {

        val i64: Expr
            get() = Load("u32i64", I64, WasmOpcode.I64_LOAD_32_U, address, align, offset)
    }

    private class Load(
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
            writer.write(opcode)
            writer.writeU32(align)
            writer.writeU32(offset)
        }

        override val returnType: List<Type>
            get() = listOf(type)

    }

}
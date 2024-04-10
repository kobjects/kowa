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


    fun i32Load(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i32Load", I32, WasmOpcode.I32_LOAD, address, align, offset)

    fun i64Load(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i64Load", I64, WasmOpcode.I64_LOAD, address, align, offset)

    fun f32Load(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("f32Load", F32, WasmOpcode.F32_LOAD, address, align, offset)

    fun f64Load(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("f64Load", F64, WasmOpcode.F64_LOAD, address, align, offset)


    fun i32load8(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i32load8", I32, WasmOpcode.I32_LOAD_8_S, address, align, offset)

    fun i32load8U(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i32load8U", I32, WasmOpcode.I32_LOAD_8_U, address, align, offset)

    fun i32load16(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i32load16", I32, WasmOpcode.I32_LOAD_16_S, address, align, offset)

    fun i32load16U(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i32load16U", I32, WasmOpcode.I32_LOAD_16_U, address, align, offset)

    fun i64load8(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i64load8", I64, WasmOpcode.I64_LOAD_8_S, address, align, offset)

    fun i64load8U(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i64load8U", I64, WasmOpcode.I64_LOAD_8_U, address, align, offset)

    fun i64load16(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i64load16", I64, WasmOpcode.I64_LOAD_16_S, address, align, offset)

    fun i64load16U(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i64load16U", I64, WasmOpcode.I64_LOAD_16_U, address, align, offset)


    fun i64load32(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i64load32", I64, WasmOpcode.I64_LOAD_32_S, address, align, offset)


    fun i64load32U(address: Any, align: Int = 0, offset: Int = 0): Expr =
        Load("i64load32U", I64, WasmOpcode.I64_LOAD_32_U, address, align, offset)


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
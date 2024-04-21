package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.binary.WasmOpcode
import org.kobjects.greenspun.binary.WasmWriter
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

        override fun toWasm(writer: WasmWriter) = writer.writeOpcode(WasmOpcode.MEMORY_SIZE)

        override val returnType: List<Type>
            get() = listOf(I32)
    }

    val i32: MemoryView
        get() = MemoryView(this, "i32", I32, WasmOpcode.I32_LOAD, WasmOpcode.I32_STORE)
    val i64: MemoryView
        get() = MemoryView(this, "i64", I64, WasmOpcode.I64_LOAD, WasmOpcode.I64_STORE)
    val f32: MemoryView
        get() = MemoryView(this, "f32", F32, WasmOpcode.F32_LOAD, WasmOpcode.F32_STORE)
    val f64: MemoryView
        get() = MemoryView(this, "f64", F64, WasmOpcode.F64_LOAD, WasmOpcode.F64_STORE)

    val s8i32: MemoryView
        get() = MemoryView(this, "s8i32", I32, WasmOpcode.I32_LOAD_8_S, WasmOpcode.I32_STORE_8)
    val u8i32: MemoryView
        get() = MemoryView(this, "u8i32", I32, WasmOpcode.I32_LOAD_8_U, WasmOpcode.I32_STORE_8)
    val s16i32: MemoryView
        get() = MemoryView(this, "s16i32", I32, WasmOpcode.I32_LOAD_16_S, WasmOpcode.I32_STORE)
    val u16i32: MemoryView
        get() = MemoryView(this, "u16i32", I32, WasmOpcode.I32_LOAD_16_U, WasmOpcode.I32_STORE)

    val s8i64: MemoryView
        get() = MemoryView(this, "s8i64", I64, WasmOpcode.I64_LOAD_8_S, WasmOpcode.I64_STORE_8)
    val u8i64: MemoryView
        get() = MemoryView(this, "u8i64", I64, WasmOpcode.I64_LOAD_8_U, WasmOpcode.I64_STORE_8)
    val s16i64: MemoryView
        get() = MemoryView(this, "s16i64", I64, WasmOpcode.I64_LOAD_16_S, WasmOpcode.I64_STORE_16)
    val u16i64: MemoryView
        get() = MemoryView(this, "u16i64", I64, WasmOpcode.I64_LOAD_16_U, WasmOpcode.I64_STORE_16)
    val s32i64: MemoryView
        get() = MemoryView(this, "s32i64", I64, WasmOpcode.I64_LOAD_32_S, WasmOpcode.I64_STORE_32)
    val u32i64: MemoryView
        get() = MemoryView(this, "u32i64", I64, WasmOpcode.I64_LOAD_32_U, WasmOpcode.I64_STORE_32)

    fun i32(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "i32", I32, WasmOpcode.I32_LOAD, WasmOpcode.I32_STORE, align, offset)

    fun i64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "i64", I64, WasmOpcode.I64_LOAD, WasmOpcode.I64_STORE)

    fun f32(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "f32", F32, WasmOpcode.F32_LOAD, WasmOpcode.F32_STORE)
    fun f64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "f64", F64, WasmOpcode.F64_LOAD, WasmOpcode.F64_STORE)

    fun s8i32(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "s8i32", I32, WasmOpcode.I32_LOAD_8_S, WasmOpcode.I32_STORE_8)
    fun u8i32(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "u8i32", I32, WasmOpcode.I32_LOAD_8_U, WasmOpcode.I32_STORE_8)
    fun s16i32(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "s16i32", I32, WasmOpcode.I32_LOAD_16_S, WasmOpcode.I32_STORE)
    fun u16i32(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "u16i32", I32, WasmOpcode.I32_LOAD_16_U, WasmOpcode.I32_STORE)

    fun s8i64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "s8i64", I64, WasmOpcode.I64_LOAD_8_S, WasmOpcode.I64_STORE_8)
    fun u8i64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "u8i64", I64, WasmOpcode.I64_LOAD_8_U, WasmOpcode.I64_STORE_8)
    fun s16i64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "s16i64", I64, WasmOpcode.I64_LOAD_16_S, WasmOpcode.I64_STORE_16)
    fun u16i64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "u16i64", I64, WasmOpcode.I64_LOAD_16_U, WasmOpcode.I64_STORE_16)
    fun s32i64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "s32i64", I64, WasmOpcode.I64_LOAD_32_S, WasmOpcode.I64_STORE_32)
    fun u32i64(align: Int = 0, offset: Int = 0) =
        MemoryView(this, "u32i64", I64, WasmOpcode.I64_LOAD_32_U, WasmOpcode.I64_STORE_32)

}

package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type

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


    fun i32Load(address: Any, a: Int = 0, b: Int = 0) = object : Expr(address) {

        init {
            require(parameterTypes() == listOf(I32))
        }

        override fun toString(writer: CodeWriter) = stringifyChildren(writer, "LoadI32", ", ", ")")

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.write(WasmOpcode.I32_LOAD)
            writer.writeU32(a)
            writer.writeU32(b)
        }

        override val returnType: List<Type>
            get() = listOf(I32)
    }

    fun i32Load8U(address: Any, a: Int = 0, b: Int = 0) = object : Expr(address) {

        init {
            require(parameterTypes() == listOf(I32))
        }

        override fun toString(writer: CodeWriter) = stringifyChildren(writer, "LoadI32", ", ", ")")

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.write(WasmOpcode.I32_LOAD_8_U)
            writer.writeU32(a)
            writer.writeU32(b)
        }

        override val returnType: List<Type>
            get() = listOf(I32)
    }

}
package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.expression.CodeWriter

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


}
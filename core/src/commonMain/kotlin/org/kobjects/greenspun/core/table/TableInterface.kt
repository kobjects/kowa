package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Exportable
import org.kobjects.greenspun.core.expression.CodeWriter
import org.kobjects.greenspun.core.type.Type

interface TableInterface : Exportable {
    val index: Int
    val type: Type
    val min: Int
    val max: Int?

    override fun writeExportDescription(writer: CodeWriter) {
        writer.write("table$index")
    }

    override fun writeExportDescription(writer: WasmWriter) {
        writer.writeByte(1)
        writer.writeU32(index)
    }
}
package org.kobjects.kowa.core.table

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.module.Exportable
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.type.Type

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
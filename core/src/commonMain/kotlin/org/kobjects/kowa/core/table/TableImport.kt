package org.kobjects.kowa.core.table

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.module.Imported
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.type.Type

class TableImport(
    override val index: Int,
    override val module: String,
    override val name: String,
    override val type: Type,
    override val min: Int,
    override val max: Int?

) : TableInterface, Imported {
    override fun writeImportDescription(writer: WasmWriter) {
        writer.writeByte(1)
        type.toWasm(writer)
        if (max == null) {
            writer.writeByte(0)
            writer.writeU32(min)
        } else {
            writer.writeByte(1)
            writer.writeU32(min)
            writer.writeU32(max)
        }
    }

    override fun writeImport(writer: CodeWriter) {
        writer.write("val table$index = ImportTable(\"$module\", \"$name\", $min")
        if (max != null) {
            writer.write(", $max")
        }
        writer.write(")")
    }
}
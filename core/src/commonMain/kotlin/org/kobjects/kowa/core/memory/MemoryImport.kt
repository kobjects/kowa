package org.kobjects.kowa.core.memory

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.module.Imported
import org.kobjects.kowa.core.expr.CodeWriter

class MemoryImport(
    override val module: String,
    override val name: String,
    override val min: Int,
    override val max: Int?
) : MemoryInterface, Imported {

    override fun writeImportDescription(writer: WasmWriter) {
        writeExportDescription(writer)
    }

    override fun writeImport(writer: CodeWriter) {
        writer.write("val memory = ImportMemory(\"$module\", \"$name\", $min")
        if (max != null) {
            writer.write(", $max")
        }
        writer.write(")")
    }
}
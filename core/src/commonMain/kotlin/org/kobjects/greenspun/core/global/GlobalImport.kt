package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.Imported
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.type.Type

class GlobalImport(
    override val index: Int,
    override val module: String,
    override val name: String,
    override val mutable: Boolean,
    override val type: Type
) : GlobalInterface, Imported {

    override fun writeImportDescription(writer: WasmWriter) {
        writer.writeByte(3)
        type.toWasm(writer)
        writer.writeByte(if (mutable) 1 else 0)
    }

    override fun writeImport(writer: CodeWriter) {
        if (mutable) {
            writer.write("val var$index = ImportVar(")
        } else {
            writer.write("val const$index = ImportConst(")
        }
        writer.write(type.toString())
    }

}
package org.kobjects.kowa.core.module

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.expr.CodeWriter

interface Imported {

    val module: String
    val name: String

    fun writeImportDescription(writer: WasmWriter)

    fun writeImport(writer: CodeWriter)
}
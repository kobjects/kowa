package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.tree.CodeWriter

interface Imported {

    val module: String
    val name: String

    fun writeImportDescription(writer: WasmWriter)

    fun writeImport(writer: CodeWriter)
}
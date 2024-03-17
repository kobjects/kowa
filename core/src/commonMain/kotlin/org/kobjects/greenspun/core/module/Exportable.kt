package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.tree.CodeWriter

interface Exportable {

    fun writeExportDescription(writer: WasmWriter)

    fun writeExportDescription(writer: CodeWriter)

}
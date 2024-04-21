package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter

interface Exportable {

    fun writeExportDescription(writer: WasmWriter)

    fun writeExportDescription(writer: CodeWriter)

}
package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expression.CodeWriter

interface Exportable {

    fun writeExportDescription(writer: WasmWriter)

    fun writeExportDescription(writer: CodeWriter)

}
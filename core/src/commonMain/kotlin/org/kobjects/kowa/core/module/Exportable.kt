package org.kobjects.kowa.core.module

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.expr.CodeWriter

interface Exportable {

    fun writeExportDescription(writer: WasmWriter)

    fun writeExportDescription(writer: CodeWriter)

}
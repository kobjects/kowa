package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmWriter

interface Exportable {

    fun writeExport(writer: WasmWriter)

}
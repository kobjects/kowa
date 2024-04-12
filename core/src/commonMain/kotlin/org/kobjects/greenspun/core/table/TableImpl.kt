package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.type.WasmType

class TableImpl(
    override val index: Int,
    override val type: WasmType,
    override val min: Int,
    override val max: Int?) : TableInterface {
}
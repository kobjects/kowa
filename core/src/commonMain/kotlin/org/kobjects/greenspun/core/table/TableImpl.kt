package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.type.Type

class TableImpl(
    override val index: Int,
    override val type: Type,
    override val min: Int,
    override val max: Int?) : TableInterface {
}
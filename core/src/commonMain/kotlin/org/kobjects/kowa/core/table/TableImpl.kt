package org.kobjects.kowa.core.table

import org.kobjects.kowa.core.type.Type

class TableImpl(
    override val index: Int,
    override val type: Type,
    override val min: Int,
    override val max: Int?) : TableInterface {
}
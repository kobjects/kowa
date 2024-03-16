package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.type.Type

class ImportedGlobal(
    override val index: Int,
    override val mutable: Boolean,
    override val type: Type
) : GlobalInterface {

}
package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Type

class Global(
    override val index: Int,
    override val mutable: Boolean,
    val initializer: Node

) : GlobalInterface {
    override val type: Type
        get() = initializer.returnType

}
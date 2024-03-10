package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.tree.SimpleNode

object Void : Type {
    override fun createConstant(value: Any): Node = None

    val None = SimpleNode("None", Void) { Unit }
}
package org.kobjects.greenspun.core.data

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.tree.LeafNode
import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.SimpleNode
import org.kobjects.greenspun.core.types.Type

object Void : Type {
    override fun createConstant(value: Any): Node = None

    val None = SimpleNode("None", Void) { Unit }
}
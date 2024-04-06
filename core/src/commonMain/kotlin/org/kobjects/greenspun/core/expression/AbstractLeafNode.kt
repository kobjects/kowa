package org.kobjects.greenspun.core.expression

abstract class AbstractLeafNode : Node() {
    final override fun children() = emptyList<Node>()

    override fun reconstruct(newChildren: List<Node>) = this
}
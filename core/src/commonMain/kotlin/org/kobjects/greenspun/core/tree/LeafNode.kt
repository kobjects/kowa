package org.kobjects.greenspun.core.tree

abstract class LeafNode : Node() {
    override fun children() = emptyList<Node>()

    override fun reconstruct(newChildren: List<Node>) = this
}
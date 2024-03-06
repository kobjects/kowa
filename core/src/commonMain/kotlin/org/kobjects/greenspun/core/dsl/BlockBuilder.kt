package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.control.Block

class BlockBuilder(variables: MutableList<Type>) : AbstractBlockBuilder(variables) {
    fun build() = Block(*statements.toTypedArray())
}
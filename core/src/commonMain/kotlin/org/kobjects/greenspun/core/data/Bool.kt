package org.kobjects.greenspun.core.data

import org.kobjects.greenspun.core.tree.SimpleNode
import org.kobjects.greenspun.core.types.Type

object Bool : Type {

    override fun createConstant(value: Any) = when(value) {
        true -> True
        false -> False
        else -> throw IllegalArgumentException("Not a boolean value: $value")
    }

    val False = SimpleNode("False", Bool) { false }

    val True = SimpleNode("True", Bool) { true }

}
package org.kobjects.greenspun.core.tree

enum class RelationalOperator {
    EQ, GE, GT, LE, LT, NE;

    override fun toString() = name.titleCase()
}
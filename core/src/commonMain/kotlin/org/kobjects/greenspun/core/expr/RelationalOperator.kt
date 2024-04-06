package org.kobjects.greenspun.core.expr

enum class RelationalOperator {
    EQ, GE, GT, LE, LT, NE;

    override fun toString() = name.titleCase()
}
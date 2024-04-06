package org.kobjects.greenspun.core.expression

enum class RelationalOperator {
    EQ, GE, GT, LE, LT, NE;

    override fun toString() = name.titleCase()
}
package org.kobjects.kowa.core.expr

enum class RelationalOperator {
    EQ, GE, GT, LE, LT, NE, GE_U, GT_U, LE_U, LT_U;

    override fun toString() = name.titleCase()
}
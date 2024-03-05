package org.kobjects.greenspun.core.types

import org.kobjects.greenspun.core.module.Func
import org.kobjects.greenspun.core.tree.Node

data class FuncType(
    val returnType: Type,
    val parameterTypes: List<Type>,
) : Type {

    override fun createConstant(value: Any): Node {
        return Func.Const(value as Func)
    }

}
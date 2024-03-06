package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.context.LocalReference
import org.kobjects.greenspun.core.control.Block
import org.kobjects.greenspun.core.module.Func
import org.kobjects.greenspun.core.types.FuncType

class FunctionBuilder(
    val moduleBuilder: ModuleBuilder,
    val returnType: Type
) : AbstractBlockBuilder(mutableListOf()) {

    internal var paramCount = 0

    fun Param(type: Type): LocalReference {

        if (paramCount != variables.size) {
            throw IllegalStateException("Parameters can't be declared after local variables.")
        }

        if (statements.isNotEmpty()) {
            throw IllegalStateException("Parameters can't be declared after statements.")
        }

        val variable = LocalReference(variables.size, type)
        variables.add(type)

        return variable
    }

    internal fun build() = Func(
        moduleBuilder.funcs.size,
        FuncType(returnType, variables.subList(0, paramCount).map { returnType }),
        variables.size,
        Block(*statements.toTypedArray())
    )
}
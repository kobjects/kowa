package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.context.LocalReference

class FunctionBuilder(
    val returnType: Type
) : BlockBuilder(mutableListOf()) {

    var paramCount = 0

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
}
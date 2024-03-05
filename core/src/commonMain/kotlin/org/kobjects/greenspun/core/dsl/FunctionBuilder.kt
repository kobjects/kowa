package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.context.LocalReference

class FunctionBuilder(
    val returnType: Type
) : BlockBuilder(mutableListOf()) {

    var parameterCount = 0

    fun Parameter(type: Type): LocalReference {

        if (parameterCount != variables.size) {
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
package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.type.Type

class ParamBuilder {

    internal val paramTypes = mutableListOf<Type>()


    fun Param(vararg type: Type) {
        paramTypes.addAll(type)
    }

    fun build(): List<Type> = paramTypes.toList()

}
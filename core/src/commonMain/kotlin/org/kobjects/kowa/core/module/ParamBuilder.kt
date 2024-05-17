package org.kobjects.kowa.core.module

import org.kobjects.kowa.core.type.Type

class ParamBuilder {

    internal val paramTypes = mutableListOf<Type>()


    fun Param(vararg type: Type) {
        paramTypes.addAll(type)
    }

    fun build(): List<Type> = paramTypes.toList()

}
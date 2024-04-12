package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.type.WasmType

class ParamBuilder {

    internal val paramTypes = mutableListOf<WasmType>()


    fun Param(vararg type: WasmType) {
        paramTypes.addAll(type)
    }

    fun build(): List<WasmType> = paramTypes.toList()

}
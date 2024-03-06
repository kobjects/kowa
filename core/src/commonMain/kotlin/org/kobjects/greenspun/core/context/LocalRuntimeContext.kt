package org.kobjects.greenspun.core.context

import org.kobjects.greenspun.core.module.Instance

class LocalRuntimeContext(
    val instance: Instance,
    size: Int = 0) {

    val variables: Array<Any> = Array(size) { Unit }

    fun getLocal(index: Int): Any = variables[index]

    fun setLocal(index: Int, value: Any) {
        variables[index] = value
    }

    fun createChild(size: Int) = LocalRuntimeContext(instance, size)

}
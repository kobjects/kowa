package org.kobjects.greenspun.core.context

class LocalRuntimeContext(size: Int = 0) {

    val variables: Array<Any> = Array(size) { Unit }

    fun getLocal(index: Int): Any = variables[index]

    fun setLocal(index: Int, value: Any) {
        variables[index] = value
        println("#0 set to $value")
    }

    fun createChild(size: Int) = LocalRuntimeContext(size)

}
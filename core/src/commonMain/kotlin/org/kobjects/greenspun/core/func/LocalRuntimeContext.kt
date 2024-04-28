package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.runtime.Instance
import org.kobjects.greenspun.runtime.Stack

class LocalRuntimeContext(
    val instance: Instance,
    val paramCount: Int = 0,
    val localCount: Int = 0,
    val stack: Stack = Stack()
) {
    val basePointer: Int

    init {
        basePointer = stack.size - paramCount
        require(stack.size >= paramCount) {
            "stack size (${stack.size}) must be greater than or equal to argument count ($paramCount)." }

        for (i in 0 until localCount) {
            stack.pushAny(Unit)
        }
    }


    fun getLocal(index: Int): Any = stack.stack[basePointer + index]

    fun setLocal(index: Int, value: Any) {
        stack.stack[basePointer + index] = value
    }

    fun createChild(paramCount: Int, localCount: Int) = LocalRuntimeContext(instance, paramCount, localCount, stack)

}
package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.module.Module

class Instance(
    val module: Module
) {
    val rootContext = LocalRuntimeContext(this)
    val globals = Array(module.globals.size) { module.globals[it].initializer.eval(rootContext) }

    init {
        module.start?.invoke(rootContext)
    }


    fun setGlobal(index: Int, value: Any) {
        globals[index] = value
    }

    fun getGlobal(index: Int): Any = globals[index]



}
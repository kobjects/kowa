package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.context.GlobalRuntimeContext
import org.kobjects.greenspun.core.context.LocalRuntimeContext

class Module(
    val funcs: List<Func>
) {
    fun createContext(): LocalRuntimeContext {
        return LocalRuntimeContext(GlobalRuntimeContext(this))
    }


}
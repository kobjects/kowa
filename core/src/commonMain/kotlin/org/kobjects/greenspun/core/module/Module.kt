package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.context.LocalRuntimeContext

class Module(
    val funcs: List<Func>,
    val globals: List<GlobalDefinition>,
    val start: Func?,
    val exports: Map<String, Func>
) {
    fun createContext(): LocalRuntimeContext {
        return LocalRuntimeContext(Instance(this))
    }


}
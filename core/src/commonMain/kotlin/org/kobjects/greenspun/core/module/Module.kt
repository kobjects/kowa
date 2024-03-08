package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.func.Func

class Module(
    val imports: Map<String, ImportFunc>,
    val funcs: List<Func>,
    val globals: List<GlobalDefinition>,
    val start: Func?,
    val exports: Map<String, Func>
) {
    fun createInstance(
        vararg imports: Pair<String, (Array<Any>) -> Any>

    ): Instance {
        val resolvedImports = Array<((Array<Any>) -> Any)?>(this.imports.size) { null }
        for (i in imports) {
            val resolved = this.imports[i.first]!!
            resolvedImports[resolved.index] = i.second
        }

        return Instance(this, resolvedImports.map { it!! } )
    }


}
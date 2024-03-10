package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.func.Func

class Module(
    val funcImports: Map<String, ImportFunc>,
    val funcs: List<Func>,
    val globals: List<GlobalDefinition>,
    val start: Func?,
    val funcExports: Map<String, Func>
) {
    fun createInstance(
        vararg funcImports: Pair<String, (Array<Any>) -> Any>

    ): Instance {
        val resolvedImports = Array<((Array<Any>) -> Any)?>(this.funcImports.size) { null }
        for (i in funcImports) {
            val resolved = this.funcImports[i.first]!!
            resolvedImports[resolved.index] = i.second
        }
        for (f in this.funcImports) {
            if (resolvedImports[f.value.index] == null) {
                throw IllegalStateException("Missing Func Import: ${f.key}")
            }
        }

        return Instance(this, resolvedImports.map { it!! } )
    }


}
package org.kobjects.greenspun.core.instance

import org.kobjects.greenspun.core.func.FuncImport
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.global.GlobalImpl
import org.kobjects.greenspun.core.memory.MemoryImport
import org.kobjects.greenspun.core.module.Module

class Instance(
    val module: Module,
    val importObject: ImportObject
) {
    val memory = if (module.memory is MemoryImport)
            importObject.memories[module.memory.module to module.memory.name]!!
        else Memory(module.memory?.min ?: 0)

    val rootContext = LocalRuntimeContext(this)
    val globals = Array(module.globals.size) {
        val global = module.globals[it]
        if (global is GlobalImpl) global.initializer.eval(rootContext) else Unit
    }

    /** func imports bound to this instance */
    val funcImports: List<Func>

    val funcExports = module.exports
        .filter { it.value is FuncInterface }
        .map { it.name to FuncExport(it.value as FuncInterface) }
        .toMap()

    init {
        val moduleFuncImports = module.funcs.filterIsInstance<FuncImport>()

        this.funcImports = List(moduleFuncImports.size) {
            val funcImport = moduleFuncImports[it]
            importObject.funcs[funcImport.module to funcImport.name] ?: throw IllegalStateException(
                "Import function ${funcImport.module}.${funcImport.name} not found.")
        }

        for (data in module.datas) {
            if (data.offset != null) {
                data.data.copyInto(memory.buffer, data.offset)
            }
        }

        if (module.start != null) {
            module.funcs[module.start].call(rootContext)
        }
    }

    fun invoke(name: String, vararg args: Any): Any {
        return funcExports[name]!!.invoke(*args)
    }


    fun setGlobal(index: Int, value: Any) {
        globals[index] = value
    }

    fun getGlobal(index: Int): Any = globals[index]


    inner class FuncExport(val func: FuncInterface) : Func {

        override operator fun invoke(vararg param: Any): Any {
            val context = rootContext.createChild(func.localContextSize)
            for (i in 0 until param.size) {
                context.setLocal(i, param[i])
            }
            return func.call(context)
        }
    }
}
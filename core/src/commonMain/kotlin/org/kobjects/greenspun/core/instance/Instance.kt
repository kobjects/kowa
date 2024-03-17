package org.kobjects.greenspun.core.instance

import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.global.GlobalImpl
import org.kobjects.greenspun.core.module.Module

class Instance(
    val module: Module,
    val imports: List<(Instance, Array<Any>) -> Any>
) {
    val memory = ByteArray(65536 * (module.memory?.min ?: 0))
    val rootContext = LocalRuntimeContext(this)
    val globals = Array(module.globals.size) {
        val global = module.globals[it]
        if (global is GlobalImpl) global.initializer.eval(rootContext) else Unit
    }

    val funcExports = module.exports
        .filter { it.value is FuncInterface }
        .map { it.name to FuncExport(it.value as FuncInterface) }
        .toMap()

    init {
        for (data in module.datas) {
            if (data.offset != null) {
                data.data.copyInto(memory, data.offset)
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


    inner class FuncExport(val func: FuncInterface) {

        operator fun invoke(vararg params: Any): Any {
            val context = rootContext.createChild(func.localContextSize)
            for (i in 0 until params.size) {
                context.setLocal(i, params[i])
            }
            return func.call(context)
        }
    }
}
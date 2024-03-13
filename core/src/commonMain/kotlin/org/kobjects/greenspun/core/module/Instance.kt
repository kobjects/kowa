package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.func.LocalRuntimeContext

class Instance(
    val module: Module,
    val imports: List<(Instance, Array<Any>) -> Any>
) {
    val memory = ByteArray(65536)
    val rootContext = LocalRuntimeContext(this)
    val globals = Array(module.globals.size) { module.globals[it].initializer.eval(rootContext) }

    val exports = module.funcExports.mapValues { ExportInstance(it.value)  }

    init {
        for (data in module.datas) {
            if (data.offset != null) {
                data.data.copyInto(memory, data.offset)
            }
        }

        module.start?.call(rootContext)
    }


    fun setGlobal(index: Int, value: Any) {
        globals[index] = value
    }

    fun getGlobal(index: Int): Any = globals[index]


    inner class ExportInstance(val func: Func) {

        operator fun invoke(vararg params: Any): Any {
            val context = rootContext.createChild(func.localContextSize)
            for (i in 0 until params.size) {
                context.setLocal(i, params[i])
            }
            return func.call(context)
        }
    }
}
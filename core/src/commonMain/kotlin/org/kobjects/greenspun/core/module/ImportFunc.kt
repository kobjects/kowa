package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.control.Callable
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.types.FuncType

class ImportFunc(
    val index: Int,
    val name: String,
    override val type: FuncType
) : Callable {

    override val localContextSize: Int
        get() = type.parameterTypes.size

    override fun call(context: LocalRuntimeContext): Any {
        return context.instance.imports[index](context.variables)
    }

    override fun toString() = "import$index"


}
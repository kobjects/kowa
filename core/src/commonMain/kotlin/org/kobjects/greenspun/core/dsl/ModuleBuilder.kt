package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.control.Block
import org.kobjects.greenspun.core.module.Func
import org.kobjects.greenspun.core.types.FuncType
import org.kobjects.greenspun.core.types.Type

class ModuleBuilder {
    val funcs = mutableListOf<Func>()

    fun Func(returnType: Type, init: FunctionBuilder.() -> Unit): Func.Const {
        val builder = FunctionBuilder(returnType)
        builder.init()
        val f = Func(
            funcs.size,
            FuncType(returnType, builder.variables.subList(0, builder.paramCount).map { returnType }),
            builder.variables.size,
            Block(*builder.statements.toTypedArray())
        )
        funcs.add(f)
        return Func.Const(f)
    }
}
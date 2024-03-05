package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.types.FuncType
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.control.Block
import org.kobjects.greenspun.core.control.Func


fun Func(returnType: Type, init: FunctionBuilder.() -> Unit): Func.Const {
    val builder = FunctionBuilder(returnType)
    builder.init()
    val f = Func(
        FuncType(returnType, builder.variables.subList(0, builder.paramCount).map { returnType }),
        builder.variables.size,
        Block(*builder.statements.toTypedArray()))

    return Func.Const(f)
}
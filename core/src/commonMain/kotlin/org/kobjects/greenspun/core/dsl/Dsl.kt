package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.module.Module


fun Module(init: ModuleBuilder.() -> Unit): Module {
    val builder = ModuleBuilder()
    builder.init()
    return Module(builder.funcs.toList())
}
package org.kobjects.kowa.core.module


fun Module(init: ModuleBuilder.() -> Unit): Module {
    val builder = ModuleBuilder()
    builder.init()
    return builder.build()
}
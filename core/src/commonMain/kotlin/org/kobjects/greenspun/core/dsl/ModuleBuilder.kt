package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.context.LocalDefinition
import org.kobjects.greenspun.core.context.LocalReference
import org.kobjects.greenspun.core.control.Block
import org.kobjects.greenspun.core.data.Void
import org.kobjects.greenspun.core.module.Func
import org.kobjects.greenspun.core.module.GlobalDefinition
import org.kobjects.greenspun.core.module.GlobalReference
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.FuncType
import org.kobjects.greenspun.core.types.Type

class ModuleBuilder {
    internal val funcs = mutableListOf<Func>()
    internal var start: Func? = null
    internal var globals = mutableListOf<GlobalDefinition>()
    internal var exports = mutableMapOf<String, Func>()

    fun Func(returnType: Type, init: FunctionBuilder.() -> Unit): Func.Const {
        val builder = FunctionBuilder(this, returnType)
        builder.init()
        val f = builder.build()
        funcs.add(f)
        return Func.Const(f)
    }

    fun Start(init: FunctionBuilder.() -> Unit) {
        start = Func(Void, init).func
    }

    private fun global(mutable: Boolean, initializerOrValue: Any): GlobalReference {
        val initializer = Node.of(initializerOrValue)
        val global = GlobalDefinition(globals.size, mutable, initializer)
        globals.add(global)
        return GlobalReference(global)
    }

    fun Var(initializerOrValue: Any) = global(true, initializerOrValue)

    fun Const(initializerOrValue: Any) = global(false, initializerOrValue)

    fun Export(name: String, f: Func.Const) {
        exports[name] = f.func
    }

    internal fun build() = Module(
        funcs.toList(), globals.toList(), start, exports.toMap())
}
package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.func.FuncBuilder
import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.Type

class ModuleBuilder {
    internal val types = mutableListOf<FuncType>()
    internal val imports = mutableListOf<ImportFunc>()
    internal val funcs = mutableListOf<Func>()
    internal var start: Func? = null
    internal var globals = mutableListOf<GlobalDefinition>()
    internal var exports = mutableMapOf<String, Func>()

    fun ImportFunc(name: String, returnType: Type, vararg paramTypes: Type): ImportFunc {
        val i = ImportFunc(imports.size, name, getFuncType(returnType, paramTypes.toList()))
        imports.add(i)
        return i
    }

    fun Func(returnType: Type, init: FuncBuilder.() -> Unit): Func.Const {
        val builder = FuncBuilder(this, returnType)
        builder.init()
        val f = builder.build()
        funcs.add(f)
        return Func.Const(f)
    }

    fun Start(init: FuncBuilder.() -> Unit) {
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
        imports.map { Pair(it.name, it) }.toMap(),
        funcs.toList(),
        globals.toList(),
        start,
        exports.toMap())

    internal fun getFuncType(returnType: Type, paramTypes: List<Type>): FuncType {
        for (candidate in types) {
            if (candidate.matches(returnType, paramTypes)) {
                return candidate
            }
        }
        val result = FuncType(types.size, returnType, paramTypes)
        types.add(result)
        return result
    }
}
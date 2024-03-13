package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.func.FuncBuilder
import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type

class ModuleBuilder {
    internal val types = mutableListOf<FuncType>()
    internal val funcImports = mutableListOf<ImportFunc>()
    internal val funcs = mutableListOf<Func>()
    internal val datas = mutableListOf<Data>()
    internal var start: Func? = null
    internal var globals = mutableListOf<GlobalDefinition>()
    internal var funcExports = mutableListOf<Func>()
    internal var activeDataAddress = 0

    fun ImportFunc(module: String, name: String, returnType: Type, vararg paramTypes: Type): ImportFunc {
        val i = ImportFunc(funcImports.size, module, name, getFuncType(returnType, paramTypes.toList()))
        funcImports.add(i)
        return i
    }

    /**
     * Defines an active data block at the 'current' address, starting with 0, incrementing the current address
     * accordingly, returning an I32 const for the data start address.
     */
    fun ActiveData(vararg data: Any) = ActiveDataAt(activeDataAddress, *data)

    fun ActiveDataAt(offset: Int, vararg data: Any): I32.Const {
        activeDataAddress = offset

        val writer = WasmWriter()
        for (item in data) {
            writer.writeAny(item)
        }

        datas.add(Data(offset, writer.toByteArray()))

        val result = I32.Const(activeDataAddress)
        activeDataAddress += writer.size
        return result
    }

    fun ExportFunc(name: String, returnType: Type, init: FuncBuilder.() -> Unit): Func.Const {
        val result = Func(name, returnType, init)
        funcExports.add(result.func)
        return result
    }


    fun Func(returnType: Type, init: FuncBuilder.() -> Unit) = Func(null, returnType, init)

    fun Func(name: String?, returnType: Type, init: FuncBuilder.() -> Unit): Func.Const {
        val builder = FuncBuilder(this, name, returnType)
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


    internal fun build() = Module(
        types.toList(),
        funcImports.toList(),
        funcs.toList(),
        globals.toList(),
        start,
        funcExports.toList(),
        datas.toList())

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
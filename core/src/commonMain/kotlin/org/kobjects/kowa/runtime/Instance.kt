package org.kobjects.kowa.runtime

import org.kobjects.kowa.core.func.FuncImport
import org.kobjects.kowa.core.func.FuncInterface
import org.kobjects.kowa.core.global.GlobalImpl
import org.kobjects.kowa.core.global.GlobalImport
import org.kobjects.kowa.core.global.GlobalInterface
import org.kobjects.kowa.core.memory.MemoryImport
import org.kobjects.kowa.core.module.Module
import org.kobjects.kowa.core.table.TableImport

class Instance(
    val module: Module,
    val importObject: ImportObject
) {
    val tables = Array<Table>(module.tables.size) {
        val table = module.tables[it]
        if (table is TableImport) importObject.tables[table.module to table.name]!!
        else Table(table.min)
    }

    val memory = if (module.memory is MemoryImport)
            importObject.memories[module.memory.module to module.memory.name]!!
        else Memory(module.memory?.min ?: 0)

    val globalImportCount = module.globals.filterIsInstance<GlobalImport>().size
    val globalValues = Array(module.globals.size - globalImportCount) {
        val global = module.globals[it + globalImportCount]
        if (global is GlobalImpl) Interpreter(global.initializer, Stack(this)).run1() else Unit
    }

    /** func imports bound to this instance */
    val funcImports: List<org.kobjects.kowa.runtime.FuncImport>
    val globalImports: List<Global>

    val funcExports = module.exports
        .filter { it.value is FuncInterface }
        .map { it.name to FuncExport(it.value as FuncInterface) }
        .toMap()

    val globalExports = module.exports
        .filter { it.value is GlobalInterface }
        .map { it.name to GlobalExport(it.value as GlobalInterface) }
        .toMap()


    init {
        val rootContext = Stack(this)

        val moduleFuncImports = module.funcs.filterIsInstance<FuncImport>()
        funcImports = List(moduleFuncImports.size) {
            val funcImport = moduleFuncImports[it]
            importObject.funcs[funcImport.module to funcImport.name] ?: throw IllegalStateException(
                "Import function ${funcImport.module}.${funcImport.name} not found.")
        }

        val moduleGlobalImports = module.globals.filterIsInstance<GlobalImport>()
        globalImports = List(moduleGlobalImports.size) {
            val globalImport = moduleGlobalImports[it]
            importObject.globals[globalImport.module to globalImport.name] ?: throw IllegalStateException(
                "Import global ${globalImport.module}.${globalImport.name} not found.")
        }

        for (element in module.elements) {
            element.funcs.copyInto(tables[element.table.index].elements, Interpreter(element.offset, rootContext).run1() as Int)
        }

        for (data in module.datas) {
            if (data.offset != null) {
                data.data.copyInto(memory.bytes, Interpreter(data.offset, rootContext).run1() as Int)
            }
        }

        if (module.start != null) {
            module.funcs[module.start].call(rootContext)
        }
    }

    fun invoke(name: String, vararg args: Any): Any {
        return funcExports[name]?.invoke(*args) ?: throw IllegalArgumentException("Function '$name' not found")
    }

    fun setGlobal(index: Int, value: Any) {
        if (!module.globals[index].mutable) {
            throw IllegalStateException("const$index is not mutable")
        }
        if (index < globalImportCount) {
            globalImports[index].value = value
        } else {
            globalValues[index - globalImportCount] = value
        }
    }

    fun getGlobal(index: Int): Any =
        if (index < globalImportCount) globalImports[index].value
        else globalValues[index - globalImportCount]

    inner class GlobalExport(global: GlobalInterface) : Global {
        val index: Int = global.index
        override var value: Any
            get() = getGlobal(index)
            set(value) = setGlobal(index, value)
    }

    inner class FuncExport(val func: FuncInterface)  {

        operator fun invoke(vararg params: Any): Any {
            val rootContext = Stack(this@Instance)
            val type = func.type
            require(params.size == type.parameterTypes.size) {
                "Parameter count (${params.size} does not match expectation ${func.type.parameterTypes.size})"
            }
            for (param in params) {
                rootContext.pushAny(param)
            }
            func.call(rootContext)
            require(rootContext.stack.size == func.type.returnType.size) {
                "Return value count ${rootContext.stack.size} does not match expectation ${func.type.returnType.size}"
            }
            return when (func.type.returnType.size) {
                0 -> Unit
                1 -> rootContext.popAny()
                else -> {
                    val result = Array<Any>(func.type.returnType.size) { rootContext.stack[it] }
                    result
                }
            }
        }
    }
}
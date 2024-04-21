package org.kobjects.greenspun.runtime

import org.kobjects.greenspun.core.func.FuncImport
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.global.GlobalImpl
import org.kobjects.greenspun.core.global.GlobalImport
import org.kobjects.greenspun.core.global.GlobalInterface
import org.kobjects.greenspun.core.memory.MemoryImport
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.table.TableImport

class Instance(
    val module: Module,
    val importObject: ImportObject
) {
    val rootContext = LocalRuntimeContext(this)

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
        if (global is GlobalImpl) Interpreter(global.initializer, rootContext).run() else Unit
    }

    /** func imports bound to this instance */
    val funcImports: List<org.kobjects.greenspun.runtime.FuncImport>
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
            element.funcs.copyInto(tables[element.table.index].elements, Interpreter(element.offset, rootContext).run() as Int)
        }

        for (data in module.datas) {
            if (data.offset != null) {
                data.data.copyInto(memory.bytes, Interpreter(data.offset, rootContext).run() as Int)
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

        operator fun invoke(vararg param: Any): Any {
            return func.call(rootContext, *param)
        }
    }
}
package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.FuncBuilder
import org.kobjects.greenspun.core.func.FuncImpl
import org.kobjects.greenspun.core.func.FuncImport
import org.kobjects.greenspun.core.global.GlobalImpl
import org.kobjects.greenspun.core.global.GlobalInterface
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.global.GlobalImport
import org.kobjects.greenspun.core.memory.MemoryImpl
import org.kobjects.greenspun.core.memory.MemoryImport
import org.kobjects.greenspun.core.memory.MemoryInterface
import org.kobjects.greenspun.core.table.TableImpl
import org.kobjects.greenspun.core.table.TableImport
import org.kobjects.greenspun.core.table.TableInterface
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type

class ModuleBuilder {
    val types = mutableListOf<FuncType>()
    val funcs = mutableListOf<FuncInterface>()
    val datas = mutableListOf<DataImpl>()
    var start: Int? = null
    var globals = mutableListOf<GlobalInterface>()
    val exports = mutableMapOf<String, Export>()
    var memory: MemoryInterface? = null
    var tables = mutableListOf<TableInterface>()

    private var memoryImplied = false
    private var activeDataAddress = 0


    fun Const(initializerOrValue: Any) = global(null, false, initializerOrValue)


    /**
     * Defines an active data block at the 'current' address, starting with 0, incrementing the current address
     * accordingly, returning an I32 const for the data start address.
     */
    fun Data(data: Any) = Data(I32.Const(activeDataAddress), data)

    fun Data(offset: Node, data: Any): DataReference {

        if (offset is I32.Const && offset.value < activeDataAddress) {
            throw IllegalArgumentException("Potentially overlapping data")
        }

        val writer = WasmWriter()
        writer.writeAny(data)

        datas.add(DataImpl(offset, writer.toByteArray()))

        val result = DataReference(offset, writer.size)

        if (offset is I32.Const) {
            activeDataAddress = offset.value + writer.size
            if (activeDataAddress > 65536 * (memory?.min ?: 0)) {
                if (memory == null || memoryImplied) {
                    memory = MemoryImpl((65535 + activeDataAddress) / 65536)
                    memoryImplied = true
                } else {
                    throw IllegalArgumentException("Data offset exceeds minimum size")
                }
            }
        }

        return result
    }

    fun ExportFunc(name: String, returnType: Type, init: FuncBuilder.() -> Unit): FuncImpl.Const {
        val result = Func(returnType, init)
        export(name, result.func)
        return result
    }

    fun Export(name: String, funcReference: FuncImpl.Const): FuncImpl.Const {
        export(name, funcReference.func)
        return funcReference
    }

    fun Export(name: String, globalReference: GlobalReference): GlobalReference {
        export(name, globalReference.global)
        return globalReference
    }

    fun Func(returnType: Type, init: FuncBuilder.() -> Unit): FuncImpl.Const {
        val builder = FuncBuilder(this, returnType)
        builder.init()
        val f = builder.build()
        funcs.add(f)
        return FuncImpl.Const(f)
    }

    fun Global(initializerOrValue: Any) = global(null, true, initializerOrValue)

    fun ImportGlobal(module: String, name: String, type: Type) = importGlobal(module, name, true, type)

    fun ImportConst(module: String, name : String, type: Type) = importGlobal(module, name, false, type)

    fun ImportFunc(module: String, name: String, returnType: Type, vararg paramTypes: Type): FuncImport {
        require (funcs.lastOrNull() !is FuncImpl) {
            "All func imports need to be declared before any function declaration."
        }
        val i = FuncImport(funcs.size, module, name, getFuncType(returnType, paramTypes.toList()))
        funcs.add(i)
        return i
    }

    fun ImportTable(module: String, name: String, type: Type, min: Int, max: Int?): Int {
        require (tables.lastOrNull() !is TableImpl) {
            "Import tables before table declarations."
        }
        val index = tables.size
        val table = TableImport(index, module, name, type, min, max)
        tables.add(table)
        return index
    }

    fun ImportMemory(module: String, name: String, min: Int, max: Int? = null) {
        if (memory != null) {
            throw IllegalStateException("multiple memories")
        }
        memory = MemoryImport(module, name, min, max)
    }
    fun Memory(min: Int, max: Int? = null) {
        if (memory != null) {
            throw IllegalStateException("multiple memories")
        }
        memory = MemoryImpl(min, max)
    }


    fun Start(func: FuncImpl.Const) {
        start = func.func.index
    }


    fun Table(type: Type, min: Int, max: Int? = null): Int {
        val index = tables.size
        val table = TableImpl(index, type, min, max)
        tables.add(table)
        return index
    }


    fun build() = Module(
        types.toList(),
        funcs.toList(),
        tables.toList(),
        memory,
        globals.toList(),
        exports.values.toList(),
        start,
        datas.toList(),
    )

   fun export(name: String, exportable: Exportable) {
        require(!exports.containsKey(name)) {
            "Export '$name' already defined."
        }
        exports.put(name, Export(name, exportable))
    }

    fun global(name: String?, mutable: Boolean, initializerOrValue: Any): GlobalReference {
        val initializer = Node.of(initializerOrValue)
        val global = GlobalImpl(globals.size, mutable, initializer)
        globals.add(global)
        if (name != null) {
            exports.put(name, Export(name, global))
        }
        return GlobalReference(global)
    }

    fun importGlobal(module: String, name: String, mutable: Boolean, type: Type): GlobalReference {
        require(globals.lastOrNull() !is GlobalImpl) {
            "All global imports must be declared before declaring global variables."
        }
        val global = GlobalImport(globals.size, module, name, mutable, type)
        globals.add(global)
        return(GlobalReference(global))
    }


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
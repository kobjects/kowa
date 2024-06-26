package org.kobjects.kowa.runtime

class ImportObject {
    val funcs = mutableMapOf<Pair<String, String>, FuncImport>()
    val memories = mutableMapOf<Pair<String, String>, Memory>()
    val globals = mutableMapOf<Pair<String, String>, Global>()
    val tables = mutableMapOf<Pair<String, String>, Table>()

    fun addFunc(module: String, name: String, func: FuncImport) {
        funcs[module to name] = func
    }

    fun addMemory(module: String, name: String, memory: Memory) {
        memories[module to name] = memory
    }

    fun addGlobal(module: String, name: String, global: Global) {
        globals[module to name] = global
    }

    fun addTable(module: String, name: String, table: Table) {
        tables[module to name] = table
    }

    // fun addGlobal(module: String, name: String, )
}
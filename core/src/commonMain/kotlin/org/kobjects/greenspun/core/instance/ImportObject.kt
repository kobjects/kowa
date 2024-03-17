package org.kobjects.greenspun.core.instance

class ImportObject {
    val funcs = mutableMapOf<Pair<String, String>, Func>()
    var memories = mutableMapOf<Pair<String, String>, Memory>()
    val globals = mutableMapOf<Pair<String, String>, Global>()

    fun addFunc(module: String, name: String, func: Func) {
        funcs[module to name] = func
    }

    fun addMemory(module: String, name: String, memory: Memory) {
        memories[module to name] = memory
    }

    fun addGlobal(module: String, name: String, global: Global) {
        globals[module to name] = global
    }

    // fun addGlobal(module: String, name: String, )
}
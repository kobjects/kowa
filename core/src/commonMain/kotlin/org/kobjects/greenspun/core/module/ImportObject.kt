package org.kobjects.greenspun.core.module

class ImportObject {
    val funcs = mutableMapOf<Pair<String, String>, (Instance, Array<Any>) -> Any>()

    fun addFunc(module: String, name: String, func: (Instance, Array<Any>) -> Any) {
        funcs[module to name] = func
    }

}
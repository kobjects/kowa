package org.kobjects.greenspun.core.module

class ImportObject {
    val funcs = mutableMapOf<Pair<String, String>, (Array<Any>) -> Any>()

    fun addFunc(module: String, name: String, func: (Array<Any>) -> Any) {
        funcs[module to name] = func
    }

}
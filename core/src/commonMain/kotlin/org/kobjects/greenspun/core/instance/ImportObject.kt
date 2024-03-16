package org.kobjects.greenspun.core.instance

class ImportObject {
    val funcs = mutableMapOf<Pair<String, String>, (Instance, Array<Any>) -> Any>()

    fun addFunc(module: String, name: String, func: (Instance, Array<Any>) -> Any) {
        funcs[module to name] = func
    }

}
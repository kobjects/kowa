package org.kobjects.kowa.runtime


fun interface FuncImport {
    operator fun invoke(instance: Instance, vararg param: Any): Any

}
package org.kobjects.greenspun.runtime


fun interface FuncImport {
    operator fun invoke(instance: Instance, vararg param: Any): Any

}
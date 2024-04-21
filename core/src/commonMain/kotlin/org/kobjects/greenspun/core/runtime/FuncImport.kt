package org.kobjects.greenspun.core.runtime


fun interface FuncImport {
    operator fun invoke(instance: Instance, vararg param: Any): Any

}
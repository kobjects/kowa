package org.kobjects.greenspun.core.instance

fun interface Func {
    operator fun invoke(vararg param: Any): Any

}
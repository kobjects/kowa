package org.kobjects.greenspun.core.runtime

fun interface Func {
    operator fun invoke(vararg param: Any): Any

}
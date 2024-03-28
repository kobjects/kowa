package org.kobjects.greenspun.core.instance

import org.kobjects.greenspun.core.tree.Idx

fun interface Func {
    operator fun invoke(vararg param: Any): Any

}
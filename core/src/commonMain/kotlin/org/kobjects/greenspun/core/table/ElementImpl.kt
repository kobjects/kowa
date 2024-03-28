package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.tree.Node

class ElementImpl(
    val tableIdx: Int,
    val offset: Node,
    vararg val funcs: FuncInterface
) {


}
package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.expression.Node

class ElementImpl(
    val tableIdx: Int,
    val offset: Node,
    vararg val funcs: FuncInterface
) {


}
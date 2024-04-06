package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.expr.Expr

class ElementImpl(
    val tableIdx: Int,
    val offset: Expr,
    vararg val funcs: FuncInterface
) {


}
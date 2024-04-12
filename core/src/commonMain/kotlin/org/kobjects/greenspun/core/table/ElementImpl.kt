package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.expr.Expr

class ElementImpl(
    val table: TableInterface,
    val offset: Expr,
    vararg val funcs: FuncInterface
) {


}
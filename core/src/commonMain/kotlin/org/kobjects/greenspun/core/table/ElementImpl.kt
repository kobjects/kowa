package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.core.binary.Wasm
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.expr.Expr

class ElementImpl(
    val table: TableInterface,
    val offset: Wasm,
    vararg val funcs: FuncInterface
) {


}
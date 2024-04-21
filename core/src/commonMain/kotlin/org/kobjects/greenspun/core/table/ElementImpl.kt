package org.kobjects.greenspun.core.table

import org.kobjects.greenspun.binary.Wasm
import org.kobjects.greenspun.core.func.FuncInterface

class ElementImpl(
    val table: TableInterface,
    val offset: Wasm,
    vararg val funcs: FuncInterface
) {


}
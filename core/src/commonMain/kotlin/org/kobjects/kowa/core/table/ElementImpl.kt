package org.kobjects.kowa.core.table

import org.kobjects.kowa.binary.Wasm
import org.kobjects.kowa.core.func.FuncInterface

class ElementImpl(
    val table: TableInterface,
    val offset: Wasm,
    vararg val funcs: FuncInterface
) {


}
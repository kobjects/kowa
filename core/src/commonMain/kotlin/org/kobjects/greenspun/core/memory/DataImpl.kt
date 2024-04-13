package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.binary.Wasm
import org.kobjects.greenspun.core.expr.Expr

class DataImpl(
    val offset: Wasm?,
    val data: ByteArray
)
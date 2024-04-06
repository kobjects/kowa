package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.expr.Expr

class DataImpl(
    val offset: Expr?,
    val data: ByteArray
)
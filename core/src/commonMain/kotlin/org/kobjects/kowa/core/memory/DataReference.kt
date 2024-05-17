package org.kobjects.kowa.core.memory

import org.kobjects.kowa.core.global.GlobalReference
import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.expr.Expr
import org.kobjects.kowa.core.type.I32

class DataReference(offset: Expr, val len: Int) : Expr(offset) {

    init {
        require(offset is I32.Const || (offset is GlobalReference && offset.returnType == listOf(I32))) {
            "Data offset must be a I32 literal or a constant global of type I32."
        }
    }



    override fun toString(writer: CodeWriter) = children[0].toString(writer)

    override fun toWasm(writer: WasmWriter) = children[0].toWasm(writer)

    override val returnType = listOf(I32)
}
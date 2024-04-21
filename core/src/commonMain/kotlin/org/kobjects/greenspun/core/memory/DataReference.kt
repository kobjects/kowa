package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.I32

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
package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.I32

class DataReference(val offset: Expr, val len: Int) : Expr() {

    init {
        require(offset is I32.Const || (offset is GlobalReference && offset.returnType == I32)) {
            "Data offset must be a I32 literal or a constant global of type I32."
        }
    }


    override fun children(): List<Expr> = listOf(offset)

    override fun toString(writer: CodeWriter) = offset.toString(writer)

    override fun toWasm(writer: WasmWriter) = offset.toWasm(writer)

    override val returnType = I32
}
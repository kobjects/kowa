package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.Type

class GlobalReference(val global: GlobalInterface) : Expr() {


    override fun toString(writer: CodeWriter) =
        writer.write("global${global.index}")

    override fun toWasm(writer: WasmWriter) {
        TODO("Not yet implemented")
    }

    override val returnType: List<Type>
        get() = listOf(global.type)
}
package org.kobjects.kowa.core.global

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.expr.Expr
import org.kobjects.kowa.core.type.Type

class GlobalReference(val global: GlobalInterface) : Expr() {


    override fun toString(writer: CodeWriter) =
        writer.write("global${global.index}")

    override fun toWasm(writer: WasmWriter) {
        TODO("Not yet implemented")
    }

    override val returnType: List<Type>
        get() = listOf(global.type)
}
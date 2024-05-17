package org.kobjects.kowa.core.expr

import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.type.Type

class InvalidExpr(val errorMessage: String) : Expr() {

    override fun toString(writer: CodeWriter) {
        writer.write("InvalidNode(", errorMessage, ")")
    }

    override fun toWasm(writer: WasmWriter) {
        throw IllegalStateException(errorMessage)
    }

    override val returnType: List<Type>
        get() = emptyList()
}
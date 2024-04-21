package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.binary.WasmWriter
import org.kobjects.greenspun.core.type.Type

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
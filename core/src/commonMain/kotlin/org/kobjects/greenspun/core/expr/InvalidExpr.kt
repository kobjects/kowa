package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

class InvalidExpr(val errorMessage: String) : AbstractLeafExpr() {
    override fun eval(context: LocalRuntimeContext): Any {
        throw IllegalStateException(errorMessage)
    }

    override fun toString(writer: CodeWriter) {
        writer.write("InvalidNode(", errorMessage, ")")
    }

    override fun toWasm(writer: WasmWriter) {
        throw IllegalStateException(errorMessage)
    }

    override val returnType: Type
        get() = Void
}
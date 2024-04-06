package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expr.AbstractLeafExpr
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter

object Void : Type {
    override fun createConstant(value: Any): Expr = None
    override fun toWasm(writer: WasmWriter) {
        writer.writeByte(WasmType.VOID.code)
    }

    object None : AbstractLeafExpr() {
   
        override fun toString(writer: CodeWriter) {
            writer.write("None")
        }

        override val returnType: Type
            get() = Void

        override fun toWasm(writer: WasmWriter) {
        }
    }

    override fun toString() = "Void"
}
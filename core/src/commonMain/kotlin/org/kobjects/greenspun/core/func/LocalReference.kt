package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.Expr
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmWriter

class LocalReference(
    val index: Int,
    val mutable: Boolean,
    override val returnType: Type
) : Expr() {
    override fun children(): List<Expr> = emptyList()

    override fun toString(writer: CodeWriter) =
        writer.write("local$index")

    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmOpcode.LOCAL_GET)
        writer.writeU32(index)
    }
}
package org.kobjects.greenspun.core.global

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.expr.AbstractLeafExpr
import org.kobjects.greenspun.core.type.Type

class GlobalReference(val global: GlobalInterface) : AbstractLeafExpr() {

    override fun eval(context: LocalRuntimeContext) =
        context.instance.getGlobal(global.index)

    override fun toString(writer: CodeWriter) =
        writer.write("global${global.index}")

    override fun toWasm(writer: WasmWriter) {
        TODO("Not yet implemented")
    }

    override val returnType: Type
        get() = global.type
}
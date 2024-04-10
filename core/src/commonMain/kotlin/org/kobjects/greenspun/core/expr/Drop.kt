package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.Type

class Drop(vararg children: Any) : Expr(*children) {

    init {
        require(parameterTypes().size > 1) {
            "More than one parameters expected (of which the last one will be dropped)."
        }
    }

    override fun toString(writer: CodeWriter) {
        stringifyChildren(writer, "Drop(", ", ")
    }

    override val returnType: List<Type>
        get() = parameterTypes().subList(0, parameterTypes().size - 2)


    override fun toWasm(writer: WasmWriter) {
        super.toWasm(writer)
        writer.write(WasmOpcode.DROP)
    }
}
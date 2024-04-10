package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.Type

class Select(vararg children: Any) : Expr(children) {

    init {
        require(parameterTypes().size == 3) {
            "Three parameter values expected"
        }
        require(parameterTypes()[0] == parameterTypes()[1]) {
            "The first parameter type ${parameterTypes()[0]} and the second parameter type ${parameterTypes()[1]} must match."
        }
        require(parameterTypes()[2] == Bool) {
            "The third parameter type must be boolean."
        }
    }

    override fun toString(writer: CodeWriter) =
        stringifyChildren(writer, "Select(")

    override val returnType: List<Type>
        get() = children.first().returnType


    override fun toWasm(writer: WasmWriter) {
        super.toWasm(writer)
        writer.write(WasmOpcode.SELECT)
    }
}
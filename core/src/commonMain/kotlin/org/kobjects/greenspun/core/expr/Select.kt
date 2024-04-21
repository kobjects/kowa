package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.binary.WasmOpcode
import org.kobjects.greenspun.binary.WasmWriter
import org.kobjects.greenspun.core.type.*

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
        if (listOf(I32, I64, F32, F64, Bool).contains(parameterTypes().first())) {
            writer.writeOpcode(WasmOpcode.SELECT)
        } else {
            writer.writeOpcode(WasmOpcode.SELECT_T)
            parameterTypes().first().toWasm(writer)
        }
    }
}
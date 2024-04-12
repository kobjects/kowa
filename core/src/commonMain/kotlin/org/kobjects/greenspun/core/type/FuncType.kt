package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.Expr

data class FuncType(
    val index: Int,
    val returnType: List<org.kobjects.greenspun.core.type.WasmType>,
    val parameterTypes: List<org.kobjects.greenspun.core.type.WasmType>,
) : org.kobjects.greenspun.core.type.WasmType {

    override fun createConstant(value: Any): Expr {
        throw UnsupportedOperationException("TODO: Re-Implement when FuncInterface is a node")
    }

    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmType.FUNC)
        writer.writeU32(parameterTypes.size)
        for (t in parameterTypes) {
            t.toWasm(writer)
        }
        writer.writeU32(returnType.size)
        for (t in returnType) {
            t.toWasm(writer)
        }
    }

    fun matches(returnType: List<org.kobjects.greenspun.core.type.WasmType>, parameterTypes: List<org.kobjects.greenspun.core.type.WasmType>) =
        this.returnType == returnType && this.parameterTypes == parameterTypes

}
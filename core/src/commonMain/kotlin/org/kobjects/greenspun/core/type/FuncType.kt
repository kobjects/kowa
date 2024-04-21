package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.binary.WasmTypeCode
import org.kobjects.greenspun.binary.WasmWriter

data class FuncType(
    val index: Int,
    val returnType: List<Type>,
    val parameterTypes: List<Type>,
) : Type {

    override fun toWasm(writer: WasmWriter) {
        writer.writeTypeCode(WasmTypeCode.FUNC)
        writer.writeU32(parameterTypes.size)
        for (t in parameterTypes) {
            t.toWasm(writer)
        }
        writer.writeU32(returnType.size)
        for (t in returnType) {
            t.toWasm(writer)
        }
    }

    fun matches(returnType: List<Type>, parameterTypes: List<Type>) =
        this.returnType == returnType && this.parameterTypes == parameterTypes

}
package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.FuncImpl
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.module.ModuleWriter

data class FuncType(
    val index: Int,
    val returnType: Type,
    val parameterTypes: List<Type>,
) : Type {

    override fun createConstant(value: Any): Node {
        throw UnsupportedOperationException("TODO: Re-Implement when FuncInterface is a node")
    }

    override fun toWasm(writer: WasmWriter) {
        writer.write(WasmType.FUNC)
        writer.writeU32(parameterTypes.size)
        for (parameterType in parameterTypes) {
            parameterType.toWasm(writer)
        }
        if (returnType == Void) {
            writer.writeU32(0)
        } else {
            writer.writeU32(1)
            returnType.toWasm(writer)
        }
    }

    fun matches(returnType: Type, parameterTypes: List<Type>) =
        this.returnType == returnType && this.parameterTypes == parameterTypes

}
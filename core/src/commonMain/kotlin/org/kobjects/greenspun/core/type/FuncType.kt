package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.Func
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.binary.WasmWriter

data class FuncType(
    val index: Int,
    val returnType: Type,
    val parameterTypes: List<Type>,
) : Type {

    override fun createConstant(value: Any): Node {
        return Func.Const(value as Func)
    }

    override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException()

    fun matches(returnType: Type, parameterTypes: List<Type>) =
        this.returnType == returnType && this.parameterTypes == parameterTypes

}
package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.control.Block
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.tree.AbstractLeafNode
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.module.ModuleWriter

class FuncImpl(
    override val index: Int,
    override val type: FuncType,
    val locals: List<Type>,
    val body: Block
) : FuncInterface {
    override val localContextSize: Int
        get() = type.parameterTypes.size + locals.size

    override fun call(context: LocalRuntimeContext) =
        body.eval(context)

    fun toString(writer: CodeWriter) {
        writer.newLine()
        writer.newLine()
        writer.write("val func$index = ")
        writer.write("Func(")
        writer.write(type.returnType)
        writer.write(") {")
        val inner = writer.indented()

        for (i in type.parameterTypes.indices) {
            inner.newLine()
            inner.write("val param$i = param(${type.parameterTypes[i]})")
        }

        body.stringifyChildren(inner)

        writer.newLine()
        writer.write("}")
    }



    // Called from the module code segment writer
    fun writeBody(writer: ModuleWriter) {
        writer.writeU32(locals.size)
        for (local in locals) {
            writer.writeU32(1)  // TODO: Add compression
            local.toWasm(writer)
        }
        body.toWasm(writer)
        writer.write(WasmOpcode.END)
    }


    class Const(val func: FuncImpl) : AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = func

        override fun toString(writer: CodeWriter) {
            writer.write("func${func.index}")
        }

        override val returnType: Type
            get() = func.type

        operator fun invoke(vararg parameters: Any) =
            Call(func, *parameters.map { of(it) }.toTypedArray() )

        override fun toWasm(writer: ModuleWriter) = throw UnsupportedOperationException()

    }


}



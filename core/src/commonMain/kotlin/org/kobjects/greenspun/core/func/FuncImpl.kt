package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.control.ReturnSignal
import org.kobjects.greenspun.core.control.Sequence
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.tree.AbstractLeafNode
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.tree.Node

class FuncImpl(
    override val index: Int,
    override val type: FuncType,
    val locals: List<Type>,
    val body: Sequence
) : FuncInterface {

    init {
        require(type.returnType == body.returnType) {
            "Declared (${type.returnType}) and actual (${body.returnType}) must match."
        }
    }

    override fun call(context: LocalRuntimeContext, vararg params: Node): Any {
        val childContext = context.createChild(type.parameterTypes.size + locals.size)
        try {
            for (i in 0 until params.size) {
                childContext.setLocal(i, params[i].eval(context))
            }
            return body.eval(childContext)
        } catch (r: ReturnSignal) {
            return r.value
        }
    }

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

        body.toString(inner)

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



}



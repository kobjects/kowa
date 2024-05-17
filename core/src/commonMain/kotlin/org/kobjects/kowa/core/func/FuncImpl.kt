package org.kobjects.kowa.core.func

import org.kobjects.kowa.binary.Wasm
import org.kobjects.kowa.core.expr.CodeWriter
import org.kobjects.kowa.core.type.Type
import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.runtime.Interpreter
import org.kobjects.kowa.core.type.FuncType
import org.kobjects.kowa.runtime.Stack

class FuncImpl(
    override val index: Int,
    override val type: FuncType,
    val locals: List<Type>,
    val body: Wasm
) : FuncInterface {


    override fun call(context: Stack) {
        context.enterFrame(type.parameterTypes.size, locals.size)
        Interpreter(body, context).run()
        context.leaveFrame(type.parameterTypes.size, locals.size)
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

        // body.toString(inner)

        writer.newLine()
        writer.write("}")
    }



    // Called from the module code segment writer
    fun writeBody(writer: WasmWriter) {
        writer.writeU32(locals.size)
        for (local in locals) {
            writer.writeU32(1)  // TODO: Add compression
            local.toWasm(writer)
        }
        writer.writeBytes(body.code)
    }



}



package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.binary.WasmWriter
import org.kobjects.greenspun.core.module.Imported
import org.kobjects.greenspun.core.expr.CodeWriter
import org.kobjects.greenspun.core.type.FuncType

class FuncImport(
    override val index: Int,
    override val module: String,
    override val name: String,
    override val type: FuncType
) : FuncInterface, Imported {


    override fun call(context: LocalRuntimeContext) {
        val params = Array<Any>(type.parameterTypes.size) {
            context.stack.peekAny(type.parameterTypes.size - it - 1)
        }
        val result = context.instance.funcImports[index](context.instance, *params)
        context.stack.replaceAny(type.parameterTypes.size, result)
    }

    override fun writeImport(writer: CodeWriter) {
        writer.newLine()
        writer.newLine()
        writer.write("val func$index = ")
        writer.write("ImportFunc(")
        writer.writeQuoted(module)
        writer.write(", ")
        writer.writeQuoted(name)
        writer.write(", ")
        writer.write(type.returnType)
        for (param in type.parameterTypes) {
            writer.write(", ")
            writer.write(param)
        }
        writer.write(")")
    }

    override fun writeImportDescription(writer: WasmWriter) {
        writer.writeByte(0)
        writer.writeU32(type.index)
    }

    override fun toString(): String {
        val writer = CodeWriter()
        writeImport(writer)
        return writer.toString()
    }


}
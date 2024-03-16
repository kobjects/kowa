package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.control.Callable
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.FuncType

class ImportFunc(
    override val index: Int,
    val module: String,
    val name: String,
    override val type: FuncType
) : Callable {

    override val localContextSize: Int
        get() = type.parameterTypes.size

    override fun call(context: LocalRuntimeContext) =
        context.instance.imports[index](context.instance, context.variables)

    override fun toString(writer: CodeWriter) {
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

    override fun toString() = "import$index"


}
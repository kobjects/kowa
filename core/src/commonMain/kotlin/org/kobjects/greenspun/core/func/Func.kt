package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.control.Call
import org.kobjects.greenspun.core.control.Callable
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.tree.AbstractLeafNode
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmWriter

class Func(
    val index: Int,
    override val type: FuncType,
    val locals: List<Type>,
    val body: Node
) : Callable {
    override val localContextSize: Int
        get() = type.parameterTypes.size + locals.size

    override fun call(context: LocalRuntimeContext) =
        body.eval(context)

    override fun getFuncIdx(module: Module) =
        index + module.funcImports.size

    // Called from the module code segment writer
    fun writeCode(writer: WasmWriter) {
        writer.writeUInt32(locals.size)
        for (local in locals) {
            writer.writeUInt32(1)  // TODO: Add compression
            local.toWasm(writer)
        }
        body.toWasm(writer)
        writer.write(WasmOpcode.END)
    }


    class Const(val func: Func) : AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = func

        override fun toString(writer: CodeWriter) {
            writer.write("func${func.index}")
        }

        override val returnType: Type
            get() = func.type

        operator fun invoke(vararg parameters: Any) =
            Call(func, *parameters.map { of(it) }.toTypedArray() )

        override fun toWasm(writer: WasmWriter) = throw UnsupportedOperationException()

    }

}



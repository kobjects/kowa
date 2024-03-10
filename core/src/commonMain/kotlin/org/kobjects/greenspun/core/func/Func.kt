package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.control.Call
import org.kobjects.greenspun.core.control.Callable
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.core.tree.AbstractLeafNode
import org.kobjects.greenspun.core.type.Type

class Func(
    val index: Int,
    override val type: FuncType,
    override val localContextSize: Int,
    val body: Node
) : Callable {

    override fun call(context: LocalRuntimeContext) =
        body.eval(context)


    class Const(val func: Func) : AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = func

        override fun toString(writer: CodeWriter) {
            writer.write("func${func.index}")
        }

        override val returnType: Type
            get() = func.type

        operator fun invoke(vararg parameters: Any) =
            Call(func, *parameters.map { it -> of(it) }.toTypedArray() )

    }

}



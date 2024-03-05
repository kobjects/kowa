package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.FuncType
import org.kobjects.greenspun.core.tree.LeafNode
import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type

class Func(
    val type: FuncType,
    val localVariableCount: Int,
    val body: Node
) {

    operator fun invoke(context: LocalRuntimeContext, vararg args: Any): Any {
        val functionContext = context.createChild(localVariableCount)
        for (i in args.indices) {
            functionContext.setLocal(i, args[i])
        }
        return body.eval(functionContext)
    }


    class Const(val func: Func) : LeafNode() {
        override fun eval(context: LocalRuntimeContext) = func

        override fun stringify(sb: StringBuilder, indent: String) {
            sb.append("F")
        }

        override val returnType: Type
            get() = func.type

        operator fun invoke(vararg parameters: Any) =
            Invoke(this, *parameters.map { it -> Node.of(it) }.toTypedArray() )

    }

}



package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.FuncType
import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type


class Invoke(
    val func: Node,
    vararg val parameters: Node
) : Node() {

    val funcType: FuncType
        get() = (func.returnType as FuncType)

    init {
        require (func.returnType is FuncType) { "not a function type"}
        val funcType = funcType
        for (i in parameters.indices) {
            require(parameters[i].returnType == funcType.returnType)
        }
    }


    override fun eval(context: LocalRuntimeContext): Any {
        val f = func.eval(context) as Func
        return f.invoke(context, *parameters.map { it.eval(context) }.toTypedArray())
    }

    override fun children(): List<Node> =
        List<Node>(parameters.size + 1) { if (it == 0) func else parameters[it - 1] }


    override fun reconstruct(newChildren: List<Node>) =
        Invoke(newChildren[0], *newChildren.subList(1, newChildren.size).toTypedArray())

    override fun stringify(sb: StringBuilder, indent: String) {
        func.stringify(sb, indent)
        sb.append('(')
        for (i in parameters.indices) {
            if (i > 0) {
                sb.append(", ")
            }
            parameters[i].stringify(sb, indent)
        }
    }

    override val returnType: Type
        get() = (func.returnType as FuncType).returnType
}
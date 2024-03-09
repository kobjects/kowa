package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.Type


class Call(
    val callable: Callable,
    vararg val parameters: Node
) : Node() {


    init {
        require(callable.type.parameterTypes.size == parameters.size) {
            "${callable.type.parameterTypes.size} parameters expected, but got ${parameters.size}"}

        for (i in parameters.indices) {
            val expectedType = callable.type.parameterTypes[i]
            val actualType = parameters[i].returnType
            require(expectedType == actualType) {
                "Type mismatch for parameter $i; expected type: $expectedType; actual type: $actualType "}
        }
    }

    override fun eval(context: LocalRuntimeContext): Any {
        val childContext = context.createChild(callable.localContextSize)
        for (i in 0 until parameters.size) {
            childContext.setLocal(i, parameters[i].eval(context))
        }
        return callable.call(childContext)
    }

    override fun children(): List<Node> = parameters.toList()

    override fun reconstruct(newChildren: List<Node>) =
        Call(callable, *newChildren.toTypedArray())

    override fun toString(writer: CodeWriter) {
        writer.write("$callable(")
        for (i in parameters.indices) {
            if (i > 0) {
                writer.write(", ")
            }
            parameters[i].toString(writer)
        }
        writer.write(")")
    }

    override val returnType: Type
        get() = callable.type.returnType
}
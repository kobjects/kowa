package org.kobjects.greenspun.core.data

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.types.Type


object Str : Type {

    operator fun invoke(value: String) = Const(value)

    override fun createConstant(value: Any) = Const(value as String)

    class Const(
        val value: String
    ): Node() {
        override fun eval(context: LocalRuntimeContext) = value

        override fun children() = listOf<Node>()

        override fun reconstruct(newChildren: List<Node>) = this

        override fun stringify(sb: StringBuilder, indent: String) {
            sb.append("Str(\"$value\")")
        }

        override val returnType: Type
            get() = Str
    }

    class Add(
        private val left: Node,
        private val right: Node,
    ) : Node() {
        override fun eval(context: LocalRuntimeContext) = left.eval(context).toString() + right.eval(context).toString()

        override fun children() = listOf(left, right)

        override fun reconstruct(newChildren: List<Node>) =
            Add(newChildren[0], newChildren[1])

        override fun stringify(sb: StringBuilder, indent: String) =
            stringifyChildren(sb, indent, "(", " + ", ")")

        override val returnType: Type
            get() = Str
    }

    override fun toString() = "Str"
}
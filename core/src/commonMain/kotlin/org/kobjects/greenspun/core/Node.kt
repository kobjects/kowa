package org.kobjects.greenspun.core

class Node<E>(
    private val name: String,
    vararg children: Evaluable<E>,
    private val op: (List<Evaluable<E>>, E) -> Any?
) : Evaluable<E> {
    val children = children.toList()

    override fun eval(env: E): Any? {
        return op(children, env)
    }

    override fun children() = children

    override fun reconstruct(newChildren: List<Evaluable<E>>) =
        Node(name, children = newChildren.toTypedArray(), op = op)

    override fun toString() =
        if (children.isEmpty()) "($name)" else "($name ${children.joinToString(" ")})"
}

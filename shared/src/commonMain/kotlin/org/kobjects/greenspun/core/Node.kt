package org.kobjects.greenspun.core

class Node<E>(
    val name: String,
    vararg children: Evaluable<E>,
    val op: (List<Evaluable<E>>, E) -> Any?
) : Evaluable<E> {
    val children = children.toList()

    override fun eval(env: E): Any? {
        return op(children, env)
    }

    override fun name() = name

    override fun children() = children
}
package org.kobjects.greenspun.core

import kotlin.reflect.KClass

class Node<E>(
    private val name: String,
    private val type: KClass<*>,
    vararg children: Evaluable<E>,
    private val op: (List<Evaluable<E>>, E) -> Any?
) : Evaluable<E> {
    val children = children.toList()

    override fun eval(env: E): Any? {
        return op(children, env)
    }

    override fun children() = children

    override fun reconstruct(newChildren: List<Evaluable<E>>) =
        Node(name, type = type, children = newChildren.toTypedArray(), op = op)

    override fun type(): KClass<*> = type

    override fun toString(indent: String) =
        "$name(${children.joinToString(", ") { it.toString(indent) }})"

}
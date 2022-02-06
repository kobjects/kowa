package org.kobjects.greenspun


class Function (
    val fn: (List<Any?>) -> Any?,
    val children: List<Evaluable>
): Evaluable {
    override fun eval(env: Env): Any? =
        fn(List(children.size) { children[it].eval(env) })
}

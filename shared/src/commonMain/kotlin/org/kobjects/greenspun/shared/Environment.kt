package org.kobjects.greenspun.shared


class Environment() {

}

interface Evaluable {
    fun eval(environment: Environment): Any?
}


class Function (
    val fn: (List<Any?>) -> Any?,
    val children: List<Evaluable>
): Evaluable {
    override fun eval(environment: Environment): Any? =
        fn(List(children.size) {children[it].eval(environment) })
}

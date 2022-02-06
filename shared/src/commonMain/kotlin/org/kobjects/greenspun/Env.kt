package org.kobjects.greenspun

/**
 * Environment management.
 */
class Env(
    val vars: MutableList<Any?>
) {

    class GetNumber(val index: Int): Evaluable {
        override fun eval(env: Env): Double {
            return env.vars[index] as Double
        }
    }

    class SetNumber(val index: Int, val expr: Evaluable): Evaluable {
        override fun eval(env: Env) {
            env.vars[index] = expr.evalNumber(env)
        }
    }


}


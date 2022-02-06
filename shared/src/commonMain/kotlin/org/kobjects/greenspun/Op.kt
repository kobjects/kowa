package org.kobjects.greenspun

/**
 * Operations.
 */
class Op {

    class Mod(val left: Evaluable, var right: Evaluable): Evaluable {
        override fun eval(env: Env): Double {
            return left.evalNumber(env) % right.evalNumber(env)
        }
    }

    class Plus(val left: Evaluable, val right: Evaluable): Evaluable {
        override fun eval(env: Env): Double {
            return left.evalNumber(env) + right.evalNumber(env)
        }
    }
}
package org.kobjects.greenspun

/**
 * Relational operations.
 */
class Rel {

    class Eq(var left: Evaluable, var right: Evaluable): Evaluable {
        override fun eval(env: Env): Boolean {
            return left.eval(env) == right.eval(env)
        }
    }

    class Ge(var left: Evaluable, var right: Evaluable): Evaluable {
        override fun eval(env: Env): Boolean {
            return left.evalNumber(env) >= right.evalNumber(env)
        }
    }

    class Gt(var left: Evaluable, var right: Evaluable): Evaluable {
        override fun eval(env: Env): Boolean {
            return left.evalNumber(env) > right.evalNumber(env)
        }
    }

    class Le(var left: Evaluable, var right: Evaluable): Evaluable {
        override fun eval(env: Env): Boolean {
            return left.evalNumber(env) <= right.evalNumber(env)
        }
    }

    class Lt(var left: Evaluable, var right: Evaluable): Evaluable {
        override fun eval(env: Env): Boolean {
            return left.evalNumber(env) < right.evalNumber(env)
        }
    }
}
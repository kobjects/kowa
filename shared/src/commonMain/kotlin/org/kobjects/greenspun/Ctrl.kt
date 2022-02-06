package org.kobjects.greenspun

/**
 * Control structures.
 */
class Ctrl {

    class If(
        val condition: Evaluable,
        val then: Evaluable,
        val otherwise: Evaluable = Evaluable.NOOP
    ) : Evaluable {
        override fun eval(env: Env): Any? {
            return if (condition.eval(env) as Boolean) then.eval(env) else otherwise.eval(env)
        }
    }


    class While(
        val condition: Evaluable,
        val body: Evaluable
    ): Evaluable {
        override fun eval(env: Env): Any? {
            while (condition.eval(env) as Boolean) {
                body.eval(env)
            }
            return null
        }
    }

    class Block(
        vararg val statements: Evaluable
    ): Evaluable {
        override fun eval(env: Env): Any? {
            for (statement: Evaluable in statements) {
                statement.eval(env)
            }
            return null
        }

    }
}
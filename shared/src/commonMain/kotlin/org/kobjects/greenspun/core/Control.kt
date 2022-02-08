package org.kobjects.greenspun.core

/**
 * Control structures.
 */
class Control {

    class If<C>(
        val condition: Evaluable<C>,
        val then: Evaluable<C>,
        val otherwise: Evaluable<C> = Block<C>()
    ) : Evaluable<C> {
        override fun eval(env: C): Any? {
            return if (condition.eval(env) as Boolean) then.eval(env) else otherwise.eval(env)
        }

        override fun children() = listOf(condition, then, otherwise)

        override fun name() = "if"
    }


    class While<C>(
        val condition: Evaluable<C>,
        val body: Evaluable<C>
    ): Evaluable<C> {
        override fun name() = "while"

        override fun eval(env: C) {
            while (condition.eval(env) as Boolean) {
                body.eval(env)
            }
        }

        override fun children() = listOf(condition, body)
    }

    class Block<C>(
        vararg val statements: Evaluable<C>
    ): Evaluable<C> {
        override fun eval(env: C) {
            for (statement: Evaluable<C> in statements) {
                statement.eval(env)
            }
        }

        override fun children() = statements.asList()

        override fun name() = "block"
    }
}
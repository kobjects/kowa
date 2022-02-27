package org.kobjects.greenspun.core

/**
 * Control structures.
 */
class Control {

    class If<C>(
        vararg val ifThenElse: Evaluable<C>
    ) : Evaluable<C> {
        override fun eval(env: C): Any? {
            for (i in ifThenElse.indices step 2) {
                if (i == ifThenElse.size - 1) {
                    return ifThenElse[i].eval(env)
                } else if (ifThenElse[i].eval(env) as Boolean) {
                    return ifThenElse[i + 1].eval(env)
                }
            }
            return Unit
        }

        override fun children() = ifThenElse.toList()

        override fun reconstruct(newChildren: List<Evaluable<C>>) = If(*newChildren.toTypedArray())

        override val type
            get() = Void

        override fun toString() ="(if ${ifThenElse.joinToString(" ")})"
    }


    class While<C>(
        val condition: Evaluable<C>,
        val body: Evaluable<C>
    ): Evaluable<C> {

        override fun eval(env: C) {
            while (condition.eval(env) as Boolean) {
                body.eval(env)
            }
        }

        override fun children() = listOf(condition, body)

        override fun reconstruct(newChildren: List<Evaluable<C>>) = While(newChildren[0], newChildren[1])

        override val type
            get() = Void

        override fun toString() = "(while $condition $body)"
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

        override fun reconstruct(newChildren: List<Evaluable<C>>) =
            Block(statements = newChildren.toTypedArray())

        override val type
            get() = if (statements.isEmpty()) Void else statements[statements.size - 1].type

        override fun toString() =
            statements.joinToString(" ", prefix = "(begin ", postfix = ")")
    }
}
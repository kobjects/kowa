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

        override fun reconstruct(newChildren: List<Evaluable<C>>) = If(newChildren[0], newChildren[1], newChildren[2])

        override val type
            get() = Void

        override fun toString(): String {
            val sb = StringBuilder("(if $condition $then")
            if (!(otherwise is Block && otherwise.statements.isEmpty())) {
                sb.append(" $otherwise")
            }
            sb.append(")")
            return sb.toString()
        }
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
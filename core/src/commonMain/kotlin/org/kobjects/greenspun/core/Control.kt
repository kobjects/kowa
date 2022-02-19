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

        override fun type() = Unit::class

        override fun toString(indent: String): String {
            val sb = StringBuilder("if ")
            sb.append(condition.toString(indent))
            sb.append(":\n  ").append(indent)
            sb.append(then.toString("  $indent"))
            if (!(otherwise is Block && otherwise.statements.isEmpty())) {
                sb.append("\n").append(indent).append("else:\n  ").append(indent)
                sb.append(otherwise.toString("  $indent"))
            }
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

        override fun type() = Unit::class

        override fun toString(indent: String) =
            "while ${condition.toString(indent)}:\n$indent${body.toString(' ' + indent + ' ')}"

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

        override fun type() = Unit::class

        override fun toString(indent: String) =
            statements.joinToString("\n$indent"){ it.toString(indent)}

    }
}
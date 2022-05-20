package org.kobjects.greenspun.core

/**
 * Control structures.
 */
class Control {

    class If<C>(
        vararg val ifThenElse: Evaluable<C>,
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

        override fun toString() ="(if ${ifThenElse.joinToString(" ")})"
    }


    class While<C>(
        val condition: Evaluable<C>,
        val body: Evaluable<C>
    ): Evaluable<C> {
        override fun eval(env: C): FlowSignal? {
            while (condition.eval(env) as Boolean) {
                val result = body.eval(env)
                if (result is FlowSignal) {
                    when (result.kind) {
                        FlowSignal.Kind.BREAK -> break
                        FlowSignal.Kind.CONTINUE -> continue
                        FlowSignal.Kind.RETURN -> return result
                    }
                }
            }
            return null
        }

        override fun children() = listOf(condition, body)

        override fun reconstruct(newChildren: List<Evaluable<C>>) = While(newChildren[0], newChildren[1])

        override fun toString() = "(while $condition $body)"
    }

    class Block<C>(
        vararg val statements: Evaluable<C>
    ): Evaluable<C> {
        override fun eval(env: C): Any? {
            var result: Any? = null
            for (statement: Evaluable<C> in statements) {
                result = statement.eval(env)
                if (result is FlowSignal) {
                    return result
                }
            }
            return result
        }

        override fun children() = statements.asList()

        override fun reconstruct(newChildren: List<Evaluable<C>>) =
            Block(statements = newChildren.toTypedArray())

        override fun toString() =
            statements.joinToString(" ", prefix = "(begin ", postfix = ")")
    }



    data class FlowSignal(
        val kind: Kind,
        val value: Any? = null) {

        enum class Kind {
            BREAK, CONTINUE, RETURN
        }

    }
}
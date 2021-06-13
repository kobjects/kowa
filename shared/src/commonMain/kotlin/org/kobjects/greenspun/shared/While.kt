package org.kobjects.greenspun.shared

class While(
    val condition: Evaluable,
    val body: Evaluable
): Evaluable {
    override fun eval(environment: Environment): Any? {
        while (condition.eval(environment) as Boolean) {
            body.eval(environment)
        }
        return null
    }
}
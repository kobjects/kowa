package org.kobjects.greenspun.shared

class If(
    val condition: Evaluable,
    val then: Evaluable,
    val otherwise: Evaluable = Evaluable.NOOP
) : Evaluable {
    override fun eval(environment: Environment): Any? {
        return if (condition.eval(environment) as Boolean) then.eval(environment) else otherwise.eval(environment)
    }
}
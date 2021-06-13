package org.kobjects.greenspun.shared

interface Evaluable {
    fun eval(environment: Environment): Any?
}
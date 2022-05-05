package org.kobjects.greenspun.core

interface Callable<C> {
    fun eval(context: C): Any?
}
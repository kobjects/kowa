package org.kobjects.greenspun.core

interface Type {
    val name: String

    fun isAssignableFrom(type: Type) = type == this
}
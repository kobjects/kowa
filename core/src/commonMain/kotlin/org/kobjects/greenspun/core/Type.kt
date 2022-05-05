package org.kobjects.greenspun.core

interface Type {
    fun isAssignableFrom(type: Type) = type == this
}
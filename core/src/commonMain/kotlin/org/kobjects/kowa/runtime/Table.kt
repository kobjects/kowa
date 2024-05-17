package org.kobjects.kowa.runtime

class Table(
    val initial: Int,
    val max: Int? = null) {

    var elements = Array<Any?>(initial) { null }

    fun grow(by: Int) {
        elements = elements.copyOf(elements.size + by)
    }
}
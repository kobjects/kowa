package org.kobjects.greenspun.core.control

data class FlowSignal(
    val kind: Kind,
    val value: Any? = null) {

    enum class Kind {
        BREAK, CONTINUE, RETURN
    }

}
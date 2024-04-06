package org.kobjects.greenspun.core.expr


fun String.titleCase(): String {
    val sb = StringBuilder()
    var toUpper = true
    for (c in this) {
        if (c == '_') {
            toUpper = true
        } else if (toUpper) {
            sb.append(c.uppercase())
            toUpper = false
        } else {
            sb.append(c.lowercase())
        }
    }
    return sb.toString()
}

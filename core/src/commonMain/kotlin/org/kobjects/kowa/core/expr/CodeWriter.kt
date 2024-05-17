package org.kobjects.kowa.core.expr

/** Glorified string builder with indent support */
class CodeWriter(
    val sb: StringBuilder = StringBuilder(),
    val indent: String = "\n"
) {
    fun indented() = CodeWriter(sb, "$indent  ")

    fun newLine() {
        sb.append(indent)
    }

    fun write(vararg values: Any) {
        for (value in values) {
            if (value is Expr) {
                value.toString(this)
            } else {
                sb.append(value)
            }
        }
    }

    override fun toString() = sb.toString()
    fun writeQuoted(s: String) {
        sb.append("\"")
        sb.append(s)
        sb.append("\"")
    }
}
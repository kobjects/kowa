package org.kobjects.greenspun.core.context

import org.kobjects.greenspun.core.tree.Node

class LocalDefinition(index: Int, expression: Node) : LocalAssignment(index, expression) {

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append("val local$index = Local(")
        expression.stringify(sb, indent)
        sb.append(")")
    }
}
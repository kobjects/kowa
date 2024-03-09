package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node

class LocalDefinition(index: Int, expression: Node) : LocalAssignment(index, expression) {

    override fun toString(writer: CodeWriter) {
        writer.write("val local$index = Local(")
        writer.write(expression)
        writer.write(')')
    }
}
package org.kobjects.greenspun.core.module

import org.kobjects.greenspun.core.context.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.LeafNode
import org.kobjects.greenspun.core.types.Type

class GlobalReference(val global: GlobalDefinition) : LeafNode() {

    override fun eval(context: LocalRuntimeContext) =
        context.instance.getGlobal(global.index)

    override fun stringify(sb: StringBuilder, indent: String) {
        sb.append("global${global.index}")
    }

    override val returnType: Type
        get() = global.initializer.returnType
}
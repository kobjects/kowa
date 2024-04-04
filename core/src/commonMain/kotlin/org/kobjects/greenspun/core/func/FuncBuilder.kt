package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.control.ReturnNode
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.control.Sequence
import org.kobjects.greenspun.core.control.SequenceBuilder
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Void

class FuncBuilder(
    moduleBuilder: ModuleBuilder,
    val returnType: Type
) : SequenceBuilder(moduleBuilder, mutableListOf()) {

    internal var paramCount = 0

    fun Param(type: Type): LocalReference {

        if (paramCount != variables.size) {
            throw IllegalStateException("Parameters can't be declared after local variables.")
        }

        if (statements.isNotEmpty()) {
            throw IllegalStateException("Parameters can't be declared after statements.")
        }

        val variable = LocalReference(variables.size, false, type)
        variables.add(type)

        paramCount++

        return variable
    }

    fun Return(value: Any = Void.None): ReturnNode {
        val node = Node.of(value)
        require(node.returnType == returnType) {
            "Return value type (${node.returnType}) does not match function return type ($returnType)."
        }
        return ReturnNode(Node.of(value))
    }

    internal fun build() = FuncImpl(
        index = moduleBuilder.funcs.size,
        type = moduleBuilder.getFuncType(returnType, variables.subList(0, paramCount)),
        locals = variables.subList(paramCount, variables.size),
        body = Sequence(*statements.toTypedArray())
    )
}
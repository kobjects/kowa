package org.kobjects.greenspun.core.dsl

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.types.Type
import org.kobjects.greenspun.core.control.Block
import org.kobjects.greenspun.core.context.LocalAssignment
import org.kobjects.greenspun.core.context.LocalDefinition
import org.kobjects.greenspun.core.context.LocalReference

open class BlockBuilder(
    val variables: MutableList<Type> = mutableListOf()
) {
    val statements = mutableListOf<Node>()

    operator fun Node.unaryPlus() = statements.add(this)

    fun Local(initializerOrValue: Any): LocalReference {
        val initializer = Node.of(initializerOrValue)
        statements.add(LocalDefinition(variables.size, initializer))
        val variable = LocalReference(variables.size, initializer.returnType)
        variables.add(initializer.returnType)
        return variable
    }

    fun Block(init: BlockBuilder.() -> Unit): Block {
        val builder = BlockBuilder(variables)
        builder.init()
        return Block(*builder.statements.toTypedArray())
    }

    fun Set(variable: LocalReference, expression: Node) =
        LocalAssignment(variable.index, expression)


}
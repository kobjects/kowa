package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalAssignment
import org.kobjects.greenspun.core.func.LocalDefinition
import org.kobjects.greenspun.core.func.LocalReference
import org.kobjects.greenspun.core.global.GlobalAssignment
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Type

abstract class AbstractBlockBuilder(val variables: MutableList<Type>) {
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
        return builder.build()
    }

    fun Set(variable: LocalReference, expression: Node): LocalAssignment {
        require(expression.returnType == variable.returnType) {
            "Expression type ${expression.returnType} does not match variable type ${variable.returnType}"
        }
        return LocalAssignment(variable.index, expression)
    }

    fun Set(variable: GlobalReference, expression: Node) =
        GlobalAssignment(variable.global, expression)

}

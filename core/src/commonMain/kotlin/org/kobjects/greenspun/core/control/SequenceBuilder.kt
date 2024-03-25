package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalAssignment
import org.kobjects.greenspun.core.func.LocalDefinition
import org.kobjects.greenspun.core.func.LocalReference
import org.kobjects.greenspun.core.global.GlobalAssignment
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.tree.BinaryOperator
import org.kobjects.greenspun.core.tree.IndirectCallNode
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.tree.Node.Companion.Not
import org.kobjects.greenspun.core.tree.RelationalOperator
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

open class SequenceBuilder(val moduleBuilder: ModuleBuilder, val variables: MutableList<Type>) {
    val statements = mutableListOf<Node>()

    operator fun Node.unaryPlus() {
        statements.add(this)
    }

    fun Local(initializerOrValue: Any): LocalReference {
        val initializer = Node.of(initializerOrValue)
        statements.add(LocalDefinition(variables.size, initializer))
        val variable = LocalReference(variables.size, initializer.returnType)
        variables.add(initializer.returnType)
        return variable
    }

    fun Block(init: SequenceBuilder.() -> Unit): BlockNode {
        val builder = SequenceBuilder(moduleBuilder, variables)
        builder.init()
        return BlockNode(builder.build())
    }

    fun CallIndirect(table: Int, index: Node, returnType: Type, vararg parameter: Node): IndirectCallNode {
        val funcType = moduleBuilder.getFuncType(returnType, parameter.toList().map { it.returnType })
        return IndirectCallNode(table, index, funcType, *parameter)
    }


    fun Loop(init: SequenceBuilder.() -> Unit): LoopNode {
        val builder = SequenceBuilder(moduleBuilder, variables)
        builder.init()
        return LoopNode(builder.build())
    }

    fun While(condition: Node, init: SequenceBuilder.() -> Unit): LoopNode {
        require(condition.returnType == Bool) {
            "While condition must be boolean"
        }
        val builder = SequenceBuilder(moduleBuilder, variables)
        builder.statements.add(BranchIf(Not(condition)))
        builder.init()
        return LoopNode(builder.build())
    }

    fun For(initialValue: Node, targetValue: Node, step: Node = I32.Const(1), init: SequenceBuilder.(Node) -> Unit): LoopNode {
        require(initialValue.returnType == I32) {
            "I32 expected for initial value."
        }
        require(targetValue.returnType == I32) {
            "I32 expected for target value."
        }
        require(step.returnType == I32) {
            "I32 expected for step value."
        }
        val loopVar = Local(initialValue)
        val builder = SequenceBuilder(moduleBuilder, variables)

        builder.statements.add(BranchIf(I32.createRelationalOperation(RelationalOperator.EQ, targetValue, loopVar)))
        builder.init(loopVar)
        builder.statements.add(Set(loopVar, I32.BinaryOperation(BinaryOperator.ADD, loopVar, step)))

        return LoopNode(builder.build())
    }


    fun If(condition: Node, init: SequenceBuilder.() -> Unit): If {
        require(condition.returnType == Bool) {
            "If condition must be boolean"
        }
        val builder = SequenceBuilder(moduleBuilder, variables)
        builder.init()
        return If(condition, builder.build(), Void.None)
    }


    fun If.Else(init: SequenceBuilder.() -> Unit): If {
        /*val last = statements.lastOrNull()
        if (last !is If) {
            throw IllegalStateException("If required for Else")
        }*/
        val builder = SequenceBuilder(moduleBuilder, variables)
        builder.init()
        /*
        val ifChildren = last.children()
        statements[statements.size] = last.reconstruct(listOf(ifChildren[0], ifChildren[1], builder.build()))
         */
        val children = children()
        return reconstruct(listOf(children[0], children[1], builder.build()))
    }


    fun Set(variable: LocalReference, expression: Node): LocalAssignment {
        require(expression.returnType == variable.returnType) {
            "Expression type ${expression.returnType} does not match variable type ${variable.returnType}"
        }
        return LocalAssignment(variable.index, expression)
    }

    fun Set(variable: GlobalReference, expression: Node) =
        GlobalAssignment(variable.global, expression)

    private fun build(): Sequence = Sequence(*statements.toTypedArray())

}

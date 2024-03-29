package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.func.LocalAssignment
import org.kobjects.greenspun.core.func.LocalDefinition
import org.kobjects.greenspun.core.func.LocalReference
import org.kobjects.greenspun.core.global.GlobalAssignment
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.tree.BinaryOperator
import org.kobjects.greenspun.core.func.IndirectCallNode
import org.kobjects.greenspun.core.table.TableInterface
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

    fun local(mutable: Boolean, initializerOrValue: Any): LocalReference {
        val initializer = Node.of(initializerOrValue)
        statements.add(LocalDefinition(variables.size, initializer))
        val variable = LocalReference(variables.size, mutable, initializer.returnType)
        variables.add(initializer.returnType)
        return variable
    }

    fun Var(initializerOrValue: Any) = local(true, initializerOrValue)

    fun Const(initializerOrValue: Any) = local(true, initializerOrValue)


    fun Block(init: SequenceBuilder.() -> Unit): BlockNode {
        val builder = SequenceBuilder(moduleBuilder, variables)
        builder.init()
        return BlockNode(builder.build())
    }

    fun CallIndirect(table: TableInterface, index: Node, returnType: Type, vararg parameter: Node): IndirectCallNode {
        val funcType = moduleBuilder.getFuncType(returnType, parameter.toList().map { it.returnType })
        return IndirectCallNode(table.index, index, funcType, *parameter)
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

    fun For(initialValue: Any, until: Any, step: Any = I32.Const(1), init: SequenceBuilder.(Node) -> Unit): LoopNode {
        val initialValueNode = Node.of(initialValue)
        val untilNode = Node.of(until)
        val stepNode = Node.of(step)

        require(initialValueNode.returnType == I32) {
            "I32 expected for initial value."
        }
        require(untilNode.returnType == I32) {
            "I32 expected for target value."
        }
        require(stepNode.returnType == I32) {
            "I32 expected for step value."
        }
        val loopVar = Var(initialValueNode)
        val builder = SequenceBuilder(moduleBuilder, variables)
        builder.statements.add(BranchIf(I32.createRelationalOperation(RelationalOperator.GE, loopVar, untilNode)))
        builder.init(loopVar)
        builder.statements.add(Set(loopVar, I32.BinaryOperation(BinaryOperator.ADD, loopVar, stepNode)))

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

package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.LocalReference
import org.kobjects.greenspun.core.global.GlobalAssignment
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.func.IndirectCallNode
import org.kobjects.greenspun.core.table.TableInterface
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type

open class SequenceBuilder(
    val moduleBuilder: ModuleBuilder,
    val variables: MutableList<Type>,
    val wasmWriter: WasmWriter) {

    private fun local(mutable: Boolean, initializer: Any): LocalReference {
        val initializerNode = Node.of(initializer)

        val variable = LocalReference(variables.size, mutable, initializerNode.returnType)
        variables.add(initializerNode.returnType)

        initializerNode.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.LOCAL_SET)
        wasmWriter.writeU32(variable.index)

        return variable
    }

    fun Var(initializerOrValue: Any) = local(true, initializerOrValue)

    fun Const(initializerOrValue: Any) = local(true, initializerOrValue)


    fun Block(init: SequenceBuilder.() -> Unit) {
        val builder = SequenceBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()
    }

    fun CallIndirect(table: TableInterface, index: Node, returnType: Type, vararg parameter: Node): IndirectCallNode {
        val funcType = moduleBuilder.getFuncType(returnType, parameter.toList().map { it.returnType })
        return IndirectCallNode(table.index, index, funcType, *parameter)
    }


    fun Loop(init: SequenceBuilder.() -> Unit) {
        wasmWriter.write(WasmOpcode.LOOP)
        val builder = SequenceBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()
        wasmWriter.write(WasmOpcode.END)
    }

    fun While(condition: Any, init: SequenceBuilder.() -> Unit) {
        val conditionNode = Node.of(condition)

        require(conditionNode.returnType == Bool) {
            "While condition must be boolean"
        }

        wasmWriter.write(WasmOpcode.BLOCK)
        wasmWriter.write(WasmType.VOID)

        wasmWriter.write(WasmOpcode.LOOP)
        wasmWriter.write(WasmType.VOID)

        conditionNode.toWasm(wasmWriter)

        wasmWriter.write(WasmOpcode.I32_EQZ)
        wasmWriter.write(WasmOpcode.BR_IF)
        wasmWriter.writeU32(1)

        val builder = SequenceBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()

        wasmWriter.write(WasmOpcode.BR)
        wasmWriter.writeU32(0)

        wasmWriter.write(WasmOpcode.END)
        wasmWriter.write(WasmOpcode.END)
    }

    fun For(initialValue: Any, until: Any, step: Any = I32.Const(1), init: SequenceBuilder.(Node) -> Unit) {
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

        wasmWriter.write(WasmOpcode.BLOCK)
        wasmWriter.write(WasmType.VOID)

        // Creates init
        val loopVar = Var(initialValueNode)

        wasmWriter.write(WasmOpcode.LOOP)
        wasmWriter.write(WasmType.VOID)

        val builder = SequenceBuilder(moduleBuilder, variables, wasmWriter)

        loopVar.toWasm(wasmWriter)
        untilNode.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.I32_GE_S)
        wasmWriter.write(WasmOpcode.BR_IF)
        wasmWriter.writeU32(1)

        builder.init(loopVar)

        loopVar.toWasm(wasmWriter)
        I32.Const(1).toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.I32_ADD)
        wasmWriter.write(WasmOpcode.BR)
        wasmWriter.write(WasmOpcode.END)
        wasmWriter.write(WasmOpcode.END)
    }


    fun If(condition: Any, init: SequenceBuilder.() -> Unit): Elseable {
        val conditionNode = Node.of(condition)
        require(conditionNode.returnType == Bool) {
            "If condition must be boolean"
        }

        conditionNode.toWasm(wasmWriter)
        val ifPosition = wasmWriter.size
        wasmWriter.write(WasmOpcode.IF)

        val builder = SequenceBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()

        val endPosition = wasmWriter.size
        wasmWriter.write(WasmOpcode.END)

        return Elseable(ifPosition, endPosition)
    }

    fun If(condition: Any, then: Any, otherwise: Any): IfNode =
        IfNode(Node.of(condition), Node.of(then), Node.of(otherwise))





    fun Set(variable: LocalReference, expression: Node) {
        require(expression.returnType == variable.returnType) {
            "Expression type ${expression.returnType} does not match variable type ${variable.returnType}"
        }
        expression.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.LOCAL_SET)
        wasmWriter.writeU32(variable.index)
    }

    fun Set(variable: GlobalReference, expression: Node) =
        GlobalAssignment(variable.global, expression)


    inner class Elseable(val ifPosition: Int, val endPosition: Int) {

        fun Else(init: SequenceBuilder.() -> Unit) {
            wasmWriter.trunc(endPosition)
            wasmWriter.openBlocks.add(ifPosition)

            wasmWriter.write(WasmOpcode.ELSE)

            val builder = SequenceBuilder(moduleBuilder, variables, wasmWriter)
            builder.init()

            wasmWriter.write(WasmOpcode.END)
        }

    }

}

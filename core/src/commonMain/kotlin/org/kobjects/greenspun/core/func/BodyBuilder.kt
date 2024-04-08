package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.Type

open class BodyBuilder(
    val moduleBuilder: ModuleBuilder,
    val variables: MutableList<Type>,
    val wasmWriter: WasmWriter) {

    private fun local(mutable: Boolean, initializer: Any): LocalReference {
        val initializerNode = Expr.of(initializer)

        require(initializerNode.returnType.size == 1) {
            "Single return value expected for variable initialization"
        }

        val variable = LocalReference(variables.size, mutable, initializerNode.returnType[0])
        variables.add(initializerNode.returnType[0])

        initializerNode.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.LOCAL_SET)
        wasmWriter.writeU32(variable.index)

        return variable
    }

    fun Var(initializerOrValue: Any) = local(true, initializerOrValue)

    fun Const(initializerOrValue: Any) = local(true, initializerOrValue)


    fun Block(init: BodyBuilder.() -> Unit) {
        val builder = BodyBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()
    }

/*

    fun TableInterface.Call(index: Expr, returnType: List<Type>, vararg parameter: Expr): Expr {
        val funcType = moduleBuilder.getFuncType(returnType, parameter.map { it.returnType })
        val node = IndirectCallExpr(this.index, index, funcType, *parameter)
        if (node.returnType.isNotEmpty()) {
            return node
        }
        node.toWasm(wasmWriter)
        return InvalidExpr("Void calls are expected to be used as statements.")
    }


 */
    fun Loop(init: BodyBuilder.() -> Unit) {
        wasmWriter.write(WasmOpcode.LOOP)
        val builder = BodyBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()
        wasmWriter.write(WasmOpcode.END)
    }

    fun While(condition: Any, init: BodyBuilder.() -> Unit) {
        val conditionNode = Expr.of(condition)

        require(conditionNode.returnType == listOf(Bool)) {
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

        val builder = BodyBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()

        wasmWriter.write(WasmOpcode.BR)
        wasmWriter.writeU32(0)

        wasmWriter.write(WasmOpcode.END)
        wasmWriter.write(WasmOpcode.END)
    }

    fun For(initialValue: Any, until: Any, step: Any = I32.Const(1), init: BodyBuilder.(Expr) -> Unit) {
        val initialValueNode = Expr.of(initialValue)
        val untilNode = Expr.of(until)
        val stepNode = Expr.of(step)

        require(initialValueNode.returnType == listOf(I32)) {
            "I32 expected for initial value."
        }
        require(untilNode.returnType == listOf(I32)) {
            "I32 expected for target value."
        }
        require(stepNode.returnType == listOf(I32)) {
            "I32 expected for step value."
        }

        // Creates init
        val loopVar = Var(initialValueNode)

        wasmWriter.write(WasmOpcode.BLOCK)
        wasmWriter.write(WasmType.VOID)

        wasmWriter.write(WasmOpcode.LOOP)
        wasmWriter.write(WasmType.VOID)

        (loopVar Ge untilNode).toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.BR_IF)
        wasmWriter.writeU32(1)

        val builder = BodyBuilder(moduleBuilder, variables, wasmWriter)
        builder.init(loopVar)

        Set(loopVar, loopVar + 1)

        wasmWriter.write(WasmOpcode.BR)
        wasmWriter.writeU32(0)

        wasmWriter.write(WasmOpcode.END)
        wasmWriter.write(WasmOpcode.END)
    }

    operator fun FuncInterface.invoke(vararg node: Any): Expr {
        val result = CallExpr(this, *node.map { Expr.of(it) }.toTypedArray())
        if (type.returnType.isNotEmpty()) {
            return result
        }
        result.toWasm(wasmWriter)
        return InvalidExpr("Void function are expected to be used as statements.")
    }

    fun If(condition: Any, init: BodyBuilder.() -> Unit): Elseable {
        val conditionNode = Expr.of(condition)
        require(conditionNode.returnType == listOf(Bool)) {
            "If condition must be boolean"
        }

        conditionNode.toWasm(wasmWriter)
        val ifPosition = wasmWriter.size
        wasmWriter.write(WasmOpcode.IF)
        wasmWriter.write(WasmType.VOID)

        val builder = BodyBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()

        val endPosition = wasmWriter.size
        wasmWriter.write(WasmOpcode.END)

        return Elseable(ifPosition, endPosition)
    }

    fun If(condition: Any, then: Any, otherwise: Any): IfExpr =
        IfExpr(Expr.of(condition), Expr.of(then), Expr.of(otherwise))





    fun Set(variable: LocalReference, value: Any) {
        val valueExpr = Expr.of(value)
        require(valueExpr.returnType == variable.returnType) {
            "Expression type ${valueExpr.returnType} does not match variable type ${variable.returnType}"
        }
        valueExpr.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.LOCAL_SET)
        wasmWriter.writeU32(variable.index)
    }

    fun Set(variable: GlobalReference, value: Any) {
        val valueExpr = Expr.of(value)
        require(valueExpr.returnType == variable.returnType) {
            "Expression type ${valueExpr.returnType} does not match variable type ${variable.returnType}"
        }
        valueExpr.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.GLOBAL_SET)
        wasmWriter.writeU32(variable.global.index)
    }


    inner class Elseable(val ifPosition: Int, val endPosition: Int) {

        fun Else(init: BodyBuilder.() -> Unit) {
            wasmWriter.trunc(endPosition)
            wasmWriter.openBlocks.add(ifPosition)

            wasmWriter.write(WasmOpcode.ELSE)

            val builder = BodyBuilder(moduleBuilder, variables, wasmWriter)
            builder.init()

            wasmWriter.write(WasmOpcode.END)
        }

    }

}

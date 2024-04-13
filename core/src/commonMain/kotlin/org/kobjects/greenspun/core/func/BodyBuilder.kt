package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmTypeCode
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.memory.MemoryView
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.table.TableInterface
import org.kobjects.greenspun.core.type.*

open class BodyBuilder(
    val moduleBuilder: ModuleBuilder,
    val blockType: BlockType,
    val parent: BodyBuilder?,
    val variables: MutableList<WasmType>,
    val wasmWriter: WasmWriter,
    val expectedReturnType: List<WasmType> = emptyList()
) {
    val stackTypes = mutableListOf<WasmType>()
    var unreachableCodePosition = -1
    val label: Label?

    var pendingLabel: Label? = null

    init {
        label = parent?.pendingLabel
        if (label != null) {
            require(label.position == wasmWriter.size) {
                "Invalid label position"
            }
            parent!!.pendingLabel = null
        }
    }


    private fun local(mutable: Boolean, initializer: Any): LocalReference {
        val initializerNode = Expr.of(initializer)

        require(initializerNode.returnType.size == 1) {
            "Single return value expected for variable initialization"
        }

        val variable = LocalReference(variables.size, mutable, initializerNode.returnType[0])
        variables.add(initializerNode.returnType[0])

        initializerNode.toWasm(wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.LOCAL_SET)
        wasmWriter.writeU32(variable.index)

        return variable
    }

    fun Var(initialValue: Any) = local(true, initialValue)

    fun Const(value: Any) = local(true, value)

    fun Drop() {
        require(stackTypes.isNotEmpty()) {
            "Nothing to drop"
        }
        wasmWriter.writeOpcode(WasmOpcode.DROP)
        stackTypes.removeLast()
    }

    fun Push(value: Any) {
        val expr = Expr.of(value)
        expr.toWasm(wasmWriter)
        stackTypes.addAll(expr.returnType)
    }

    fun Block(init: BodyBuilder.() -> Unit) {
        val builder = BodyBuilder(moduleBuilder, BlockType.BLOCK,this, variables, wasmWriter)
        builder.init()
    }

    fun Br(target: Label) {
        var targetBlockBuilder: BodyBuilder = this
        var depth = 0
        while (targetBlockBuilder.label != target) {
            depth++
            require (targetBlockBuilder.parent != null) {
                "Label not found."
            }
            targetBlockBuilder = targetBlockBuilder.parent!!
        }

        if (targetBlockBuilder.blockType == BlockType.LOOP) {
            require(stackTypes.isEmpty()) {
                "Empty stack expected for loop target"
            }
        } else {
            require(stackTypes == targetBlockBuilder.expectedReturnType) {
                "Stack contents ($stackTypes) do not match expectations for target block (${targetBlockBuilder.expectedReturnType})"
            }
        }

        wasmWriter.writeOpcode(WasmOpcode.BR)
        wasmWriter.writeU32(depth)
        unreachableCodePosition = wasmWriter.size
    }

    operator fun TableInterface.invoke(i: Any, type: FuncType, vararg param: Any): Expr {
        val paramExpr = param.map { Expr.of(param) }
        val paramTypes = paramExpr.map { it.returnType }.flatten()
        require(paramTypes == type.parameterTypes) {
            "Actual parameter types ($paramTypes) do not match expected parameter types (${type.parameterTypes})"
        }

        val result = IndirectCallExpr(index, Expr.of(i), type, *paramExpr.toTypedArray())
        if (type.returnType.isNotEmpty()) {
            return result
        }
        result.toWasm(wasmWriter)
        return InvalidExpr("Void function are expected to be used as statements.")
    }

    fun Loop(init: BodyBuilder.() -> Unit) {
        wasmWriter.writeOpcode(WasmOpcode.LOOP)
        val builder = BodyBuilder(moduleBuilder, BlockType.LOOP, this, variables, wasmWriter)
        builder.init()
        wasmWriter.writeOpcode(WasmOpcode.END)
    }

    fun While(condition: Any, init: BodyBuilder.() -> Unit) {
        val conditionNode = Expr.of(condition)

        require(conditionNode.returnType == listOf(Bool)) {
            "While condition must be boolean"
        }

        wasmWriter.writeOpcode(WasmOpcode.BLOCK)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        wasmWriter.writeOpcode(WasmOpcode.LOOP)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        conditionNode.toWasm(wasmWriter)

        wasmWriter.writeOpcode(WasmOpcode.I32_EQZ)
        wasmWriter.writeOpcode(WasmOpcode.BR_IF)
        wasmWriter.writeU32(1)

        val outerBuilder = BodyBuilder(moduleBuilder, BlockType.BLOCK, this, variables, wasmWriter)
        val builder = BodyBuilder(moduleBuilder, BlockType.LOOP, outerBuilder, variables, wasmWriter)
        builder.init()
        builder.close()

        wasmWriter.writeOpcode(WasmOpcode.BR)
        wasmWriter.writeU32(0)

        wasmWriter.writeOpcode(WasmOpcode.END)
        wasmWriter.writeOpcode(WasmOpcode.END)
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

        wasmWriter.writeOpcode(WasmOpcode.BLOCK)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        wasmWriter.writeOpcode(WasmOpcode.LOOP)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        (loopVar Ge untilNode).toWasm(wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.BR_IF)
        wasmWriter.writeU32(1)

        val outerBuilder = BodyBuilder(moduleBuilder, BlockType.BLOCK, this, variables, wasmWriter)
        val builder = BodyBuilder(moduleBuilder, BlockType.LOOP, outerBuilder, variables, wasmWriter)
        builder.init(loopVar)

        loopVar.set(loopVar + 1)

        wasmWriter.writeOpcode(WasmOpcode.BR)
        wasmWriter.writeU32(0)

        wasmWriter.writeOpcode(WasmOpcode.END)
        wasmWriter.writeOpcode(WasmOpcode.END)
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
        wasmWriter.writeOpcode(WasmOpcode.IF)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        val builder = BodyBuilder(moduleBuilder, BlockType.IF, this, variables, wasmWriter)
        builder.init()
        builder.close()

        val endPosition = wasmWriter.size
        wasmWriter.writeOpcode(WasmOpcode.END)

        return Elseable(ifPosition, endPosition)
    }

    fun If(condition: Any, then: Any, otherwise: Any): IfExpr =
        IfExpr(Expr.of(condition), Expr.of(then), Expr.of(otherwise))


    fun LocalReference.set(value: Any) {
        val valueExpr = Expr.of(value)
        require(valueExpr.returnType == returnType) {
            "Expression type ${valueExpr.returnType} does not match variable type ${returnType}"
        }
        valueExpr.toWasm(wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.LOCAL_SET)
        wasmWriter.writeU32(index)
    }

    fun GlobalReference.set(value: Any) {
        val valueExpr = Expr.of(value)
        require(valueExpr.returnType == returnType) {
            "Expression type ${valueExpr.returnType} does not match variable type ${returnType}"
        }
        valueExpr.toWasm(wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.GLOBAL_SET)
        wasmWriter.writeU32(global.index)
    }


    operator fun MemoryView.set(address: Any, value: Any) =
        set(address, 0, 0, value)

    operator fun MemoryView.set(address: Any, align: Int, value: Any) =
        set(address, align, 0, value)

    operator fun MemoryView.set(address: Any, align: Int, offset: Int, value: Any) {
        val valueExpr = Expr.of(value)
        val valueType = valueExpr.returnType

        require (valueType == listOf(type)) {
            "For $name, the value type must be $type instead of $valueType"
        }

        Expr.of(address).toWasm(wasmWriter)
        valueExpr.toWasm(wasmWriter)

        wasmWriter.writeOpcode(storeOpcode)
        wasmWriter.writeU32(align)
        wasmWriter.writeU32(offset)
    }


    fun close(expectedStackTypes: List<WasmType> = emptyList()): List<WasmType> {
        require(wasmWriter.size == unreachableCodePosition || expectedStackTypes == stackTypes) {
            "Stack contents ($stackTypes) do not match expected types ($expectedStackTypes)"
        }

        require (pendingLabel == null) {
            "Invalid label position. Labels must immediately precede blocks."
        }

        return expectedStackTypes
    }


    inner class Elseable(val ifPosition: Int, val endPosition: Int) {

        fun Else(init: BodyBuilder.() -> Unit) {
            wasmWriter.trunc(endPosition)
            wasmWriter.openBlocks.add(ifPosition)

            wasmWriter.writeOpcode(WasmOpcode.ELSE)

            val builder = BodyBuilder(moduleBuilder, BlockType.IF, this@BodyBuilder, variables, wasmWriter)
            builder.init()
            builder.close()

            wasmWriter.writeOpcode(WasmOpcode.END)
        }

    }

    inner class Label() {
        val position = wasmWriter.size

        init {
            pendingLabel = this
        }
    }


    enum class BlockType {
        LOOP, BLOCK, IF, FUNCTION
    }

}

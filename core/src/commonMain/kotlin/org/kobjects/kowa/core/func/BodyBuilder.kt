package org.kobjects.kowa.core.func

import org.kobjects.kowa.binary.WasmTypeCode
import org.kobjects.kowa.binary.WasmOpcode
import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.expr.*
import org.kobjects.kowa.core.global.GlobalReference
import org.kobjects.kowa.core.memory.MemoryView
import org.kobjects.kowa.core.module.ModuleBuilder
import org.kobjects.kowa.core.table.TableInterface
import org.kobjects.kowa.core.type.*

open class BodyBuilder(
    val moduleBuilder: ModuleBuilder,
    val blockType: BlockType,
    val parent: BodyBuilder?,
    val variables: MutableList<Type>,
    val wasmWriter: WasmWriter,
    val expectedReturnType: List<Type> = emptyList(),
    val stackTypes: MutableList<Type> = mutableListOf()
) {
    var unreachableCodePosition = -1
    val label: Label?

    var pendingLabel: Label? = null

    init {
        label = parent?.pendingLabel?.attach(this)
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

    fun Drop(vararg values: Any) {
        if (values.isEmpty()) {
            require(stackTypes.isNotEmpty()) {
                "Nothing to drop"
            }
            wasmWriter.writeOpcode(WasmOpcode.DROP)
            stackTypes.removeLast()
        } else {
            for (value in values) {
                val valueExpr = Expr.of(value)
                valueExpr.toWasm(wasmWriter)
                for (type in valueExpr.returnType) {
                    wasmWriter.writeOpcode(WasmOpcode.DROP)
                }
            }
        }
    }

    fun Push(value: Any) {
        val expr = Expr.of(value)
        expr.toWasm(wasmWriter)
        stackTypes.addAll(expr.returnType)
    }

    operator fun Expr.unaryPlus() {
        Push(this)
    }

    fun Block(init: BodyBuilder.() -> Unit) {
        val builder = BodyBuilder(moduleBuilder, BlockType.BLOCK,this, variables, wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.BLOCK)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)
        builder.init()
        builder.close()
        wasmWriter.writeOpcode(WasmOpcode.END)
    }

    fun Block(returnType: ValueType, init: BodyBuilder.() -> Unit) =
        BlockExpr(
            WasmOpcode.BLOCK,
            init,
            returnType,
            BodyBuilder(moduleBuilder,
                BlockType.BLOCK,
                this,
                variables,
                wasmWriter,
                listOf(returnType)))

    fun Block(type: FuncType, init: BodyBuilder.() -> Unit, vararg args: Any): Expr {
        val params = Array(args.size) { Expr.of(args[it]) }
        val paramTypes = mutableListOf<Type>()
        for (p in params) {
            paramTypes.addAll(p.returnType)
            p.toWasm(wasmWriter)
        }
        require(paramTypes == type.parameterTypes) {
            "Actual parameter types $paramTypes don't match expected parameter types ${type.parameterTypes}"
        }
        val expr = BlockExpr(WasmOpcode.BLOCK, init, type,
            BodyBuilder(moduleBuilder, BlockType.BLOCK, this, variables, wasmWriter, type.returnType, paramTypes))
        if (type.returnType.isNotEmpty()) {
            return expr
        }
        expr.toWasm(wasmWriter)
        return InvalidExpr("Block does not return any value.")
    }

    fun Loop(type: FuncType, init: BodyBuilder.() -> Unit, vararg args: Any): Expr {
        val params = Array(args.size) { Expr.of(args[it]) }
        val paramTypes = mutableListOf<Type>()
        for (p in params) {
            paramTypes.addAll(p.returnType)
            p.toWasm(wasmWriter)
        }
        require(paramTypes == type.parameterTypes) {
            "Actual parameter types $paramTypes don't match expected parameter types ${type.parameterTypes}"
        }
        val expr = BlockExpr(WasmOpcode.BLOCK, init, type,
            BodyBuilder(moduleBuilder, BlockType.BLOCK, this, variables, wasmWriter, type.returnType, paramTypes))
        if (type.returnType.isNotEmpty()) {
            return expr
        }
        expr.toWasm(wasmWriter)
        return InvalidExpr("Block does not return any value.")
    }

    fun Loop(returnType: ValueType, init: BodyBuilder.() -> Unit) =
        BlockExpr(
            WasmOpcode.LOOP,
            init,
            returnType,
            BodyBuilder(moduleBuilder,
                BlockType.LOOP,
                this,
                variables,
                wasmWriter,
                listOf(returnType)))


    class BlockExpr(
        val opcode: WasmOpcode,
        val init: BodyBuilder.() -> Unit,
        val type: Type,
        val builder: BodyBuilder
    ) : Expr() {

        override fun toString(writer: CodeWriter) {
           writer.write(opcode.name, "(", returnType.first(), ")")
           writer.write("{")
           writer.write(builder)
           writer.write("}")
        }

        override val returnType: List<Type>
            get() = if (type is FuncType) type.returnType else listOf(type)

        override fun toWasm(writer: WasmWriter) {
            super.toWasm(writer)
            writer.writeOpcode(opcode)
            type.toWasm(writer)
            builder.init()
            builder.close()
            writer.writeOpcode(WasmOpcode.END)
        }
    }

    fun checkStackForJumpTarget(target: BodyBuilder) {
        if (target.blockType == BlockType.LOOP) {
            require(stackTypes.isEmpty()) {
                "Empty stack expected for loop target"
            }
        } else {
            require(stackTypes == target.expectedReturnType) {
                "Stack contents ($stackTypes) do not match expectations for target block (${target.expectedReturnType})"
            }
        }
    }

    fun Br(target: Label, vararg returnValue: Any) {
        var depth = target.distanceFrom(this)

        for (value in returnValue) {
            val valueExpr = Expr.of(value)
            valueExpr.toWasm(wasmWriter)
            stackTypes.addAll(valueExpr.returnType)
        }

        checkStackForJumpTarget(target.target!!)

        wasmWriter.writeOpcode(WasmOpcode.BR)
        wasmWriter.writeU32(depth)
        unreachableCodePosition = wasmWriter.size
    }

    fun BrIf(target: Label, condition: Any) {
        var depth = target.distanceFrom(this)

        checkStackForJumpTarget(target.target!!)

        val conditionExpr = Expr.of(condition)
        require(conditionExpr.returnType == listOf(Bool) || conditionExpr.returnType == listOf(I32)) {
            "Condition type must be Bool or I32, but was ${conditionExpr.returnType}"
        }
        conditionExpr.toWasm(wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.BR_IF)
        wasmWriter.writeU32(depth)
    }

    fun BrTable(index: Any, defaultLabel: Label, vararg labels: Label) {
        checkStackForJumpTarget(defaultLabel.target!!)
        val defaultIndex = defaultLabel.distanceFrom(this)
        val indices = IntArray(labels.size) {
            val label = labels[it]
            checkStackForJumpTarget(label.target!!)
            label.distanceFrom(this)
        }

        val indexExpr = Expr.of(index)
        require(indexExpr.returnType == listOf(I32)) {
            "I32 index expression required, but actual type is ${indexExpr.returnType}"
        }
        indexExpr.toWasm(wasmWriter)

        wasmWriter.writeOpcode(WasmOpcode.BR_TABLE)
        wasmWriter.writeU32(indices.size)
        for (index in indices) {
            wasmWriter.writeU32(index)
        }
        wasmWriter.writeU32(defaultIndex)
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
        val builder = BodyBuilder(moduleBuilder, BlockType.LOOP, this, variables, wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.LOOP)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)
        builder.init()
        builder.close()
        wasmWriter.writeOpcode(WasmOpcode.END)
    }

    fun While(condition: Any, init: BodyBuilder.() -> Unit) {
        val conditionNode = Expr.of(condition)

        require(conditionNode.returnType == listOf(Bool)) {
            "While condition must be boolean"
        }

        // We need to create the builder before the firs write to capture a potential label correctly.
        val outerBuilder = BodyBuilder(moduleBuilder, BlockType.BLOCK, this, variables, wasmWriter)

        wasmWriter.writeOpcode(WasmOpcode.BLOCK)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        wasmWriter.writeOpcode(WasmOpcode.LOOP)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        conditionNode.toWasm(wasmWriter)

        wasmWriter.writeOpcode(WasmOpcode.I32_EQZ)
        wasmWriter.writeOpcode(WasmOpcode.BR_IF)
        wasmWriter.writeU32(1)

        val builder = BodyBuilder(moduleBuilder, BlockType.LOOP, outerBuilder, variables, wasmWriter)
        builder.init()
        builder.close()

        wasmWriter.writeOpcode(WasmOpcode.BR)
        wasmWriter.writeU32(0)

        wasmWriter.writeOpcode(WasmOpcode.END)
        wasmWriter.writeOpcode(WasmOpcode.END)
    }

    fun For(initialValue: Any, until: Any, step: Any? = null, init: BodyBuilder.(Expr) -> Unit) {
        val initialValueExpr = Expr.of(initialValue)
        val untilExpr = Expr.of(until)
        val type = initialValueExpr.returnType.first()
        val stepExpr = Expr.of(step ?: if (type == I32) Const(1) else Const(1L))

        require(type == I32 || type == I64) {
            "I32 or I64 expected for initial value."
        }
        require(untilExpr.returnType == initialValueExpr.returnType) {
            "${initialValueExpr.returnType} expected for target value instead of ${untilExpr.returnType}."
        }
        require(stepExpr.returnType == initialValueExpr.returnType) {
            "${initialValueExpr.returnType} expected for step value instead of ${stepExpr.returnType}."
        }

        // Creates init
        val loopVar = Var(initialValueExpr)

        // We need to create the builder before the firs write to capture a potential label correctly.
        val outerBuilder = BodyBuilder(moduleBuilder, BlockType.BLOCK, this, variables, wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.BLOCK)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        wasmWriter.writeOpcode(WasmOpcode.LOOP)
        wasmWriter.writeTypeCode(WasmTypeCode.VOID)

        (loopVar Ge untilExpr).toWasm(wasmWriter)
        wasmWriter.writeOpcode(WasmOpcode.BR_IF)
        wasmWriter.writeU32(1)

        val builder = BodyBuilder(moduleBuilder, BlockType.LOOP, outerBuilder, variables, wasmWriter)
        builder.init(loopVar)

        loopVar.set(loopVar + stepExpr)

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


    fun close(): List<Type> {
        require(unreachableCodePosition != -1 || expectedReturnType == stackTypes) {
            "Stack contents ($stackTypes) do not match expected types ($expectedReturnType) at $blockType end"
        }

        require (pendingLabel == null) {
            "Invalid label position. Labels must immediately precede blocks."
        }

        return expectedReturnType
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
        var target: BodyBuilder? = null

        init {
            pendingLabel = this
        }


        fun attach(target: BodyBuilder): Label {
            require (this.target == null) {
                "Label is already attached to ${this.target}"
            }
            require(position == target.wasmWriter.size) {
                 "Invalid label position"
            }
            pendingLabel = null
            this.target = target
            return this
        }


        fun distanceFrom(source: BodyBuilder): Int {
            var distance = 0
            var current = source
            while (current.label != this) {
                distance++
                require (current.parent != null) {
                    "Label not found."
                }
                current = current.parent!!
            }
            require(current == target) {
                "Internal inconsistency"
            }
            return distance
        }

    }


    enum class BlockType {
        LOOP, BLOCK, IF, FUNCTION
    }

}

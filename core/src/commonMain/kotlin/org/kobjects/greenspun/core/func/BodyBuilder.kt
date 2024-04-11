package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.expr.*
import org.kobjects.greenspun.core.global.GlobalReference
import org.kobjects.greenspun.core.memory.MemoryInterface
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.table.TableInterface
import org.kobjects.greenspun.core.type.*

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

    fun Var(initialValue: Any) = local(true, initialValue)

    fun Const(value: Any) = local(true, value)


    fun Block(init: BodyBuilder.() -> Unit) {
        val builder = BodyBuilder(moduleBuilder, variables, wasmWriter)
        builder.init()
    }


    operator fun TableInterface.EntryRef.invoke(vararg param: Any): IndirectCallExpr {
        val paramExpr = param.map { Expr.of(param) }
        val paramTypes = paramExpr.map { it.returnType }.flatten()
        val funcType = moduleBuilder.getFuncType(returnType, paramTypes)

        return IndirectCallExpr(table.index, Expr.of(i), funcType, *paramExpr.toTypedArray())
    }

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

        loopVar.set(loopVar + 1)

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


    fun LocalReference.set(value: Any) {
        val valueExpr = Expr.of(value)
        require(valueExpr.returnType == returnType) {
            "Expression type ${valueExpr.returnType} does not match variable type ${returnType}"
        }
        valueExpr.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.LOCAL_SET)
        wasmWriter.writeU32(index)
    }

    fun GlobalReference.set(value: Any) {
        val valueExpr = Expr.of(value)
        require(valueExpr.returnType == returnType) {
            "Expression type ${valueExpr.returnType} does not match variable type ${returnType}"
        }
        valueExpr.toWasm(wasmWriter)
        wasmWriter.write(WasmOpcode.GLOBAL_SET)
        wasmWriter.writeU32(global.index)
    }

    operator fun MemoryInterface.set(address: Any, align: Int, value: Any) =
        store(address, align, 0, value)

    operator fun MemoryInterface.set(address: Any, value: Any) =
        store(address, 0, 0, value)

    operator fun MemoryInterface.set(address: Any, align: Int, offset: Int, value: Any) =
        store(address, 0, 0, value)

    fun MemoryInterface.store(address: Any, align: Int, value: Any) =
        store(address, align, 0, value)

    fun MemoryInterface.store(address: Any, value: Any) =
        store(address, 0, 0, value)

    fun MemoryInterface.store(address: Any, align: Int, offset: Int, value: Any) {
        store(
            "store", address, align, offset, value, mapOf(
                Bool to WasmOpcode.I32_STORE,
                I32 to WasmOpcode.I32_STORE,
                I64 to WasmOpcode.I64_STORE,
                F32 to WasmOpcode.F32_STORE,
                F64 to WasmOpcode.F64_STORE)
        )
    }

    fun MemoryInterface.store8(address: Any, value: Any) =
        store8(address, 0, 0, value)

    fun MemoryInterface.store8(address: Any, align: Int, value: Any) =
        store8(address, align, 0, value)

    fun MemoryInterface.store8(address: Any, align: Int, offset: Int, value: Any) {
        store(
            "store8", address, align, offset, value, mapOf(
                Bool to WasmOpcode.I32_STORE_8,
                I32 to WasmOpcode.I32_STORE_8,
                I64 to WasmOpcode.I64_STORE_8)
        )
    }

    fun MemoryInterface.store16(address: Any, value: Any) =
        store16(address, 0, 0, value)

    fun MemoryInterface.store16(address: Any, align: Int, value: Any) =
        store16(address, align, 0, value)

    fun MemoryInterface.store16(address: Any, align: Int, offset: Int, value: Any) {
        store(
            "store16", address, align, offset, value, mapOf(
                Bool to WasmOpcode.I32_STORE_16,
                I32 to WasmOpcode.I32_STORE_16,
                I64 to WasmOpcode.I64_STORE_16)
        )
    }


    fun MemoryInterface.store32(address: Any, value: Any) =
        store32(address, 0, 0, value)

    fun MemoryInterface.store32(address: Any, align: Int, value: Any) =
        store32(address, align, 0, value)

    fun MemoryInterface.store32(address: Any, align: Int, offset: Int, value: Any) {
        store(
            "store32", address, align, offset, value, mapOf(
                I64 to WasmOpcode.I64_STORE_32)
        )
    }

    private fun store(name: String, address: Any, align: Int, offset: Int, value: Any, opcodeMap: Map<Type, WasmOpcode>) {
        val valueExpr = Expr.of(value)
        val type = valueExpr.returnType

        require (type.size == 1 && opcodeMap.containsKey(type.first())) {
            "For this $name, the value type must be one of ${opcodeMap.keys}"
        }

        Expr.of(address).toWasm(wasmWriter)
        valueExpr.toWasm(wasmWriter)

        wasmWriter.write(opcodeMap[type.first()]!!)
        wasmWriter.writeU32(align)
        wasmWriter.writeU32(offset)
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

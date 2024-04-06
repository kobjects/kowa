package org.kobjects.greenspun.core.expression

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.storeF64
import org.kobjects.greenspun.core.binary.storeI32
import org.kobjects.greenspun.core.binary.storeI64
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.*

class Store(val target: Node, val value: Node) : Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        val memory = context.instance.memory
        val address = target.evalI32(context)
        when (value.returnType) {
            Bool -> memory.buffer.storeI32(address, if (value.evalBool(context)) 1 else 0)
            I32 -> memory.buffer.storeI32(address, value.evalI32(context))
            I64 -> memory.buffer.storeI64(address, value.evalI64(context))
            F64 -> memory.buffer.storeF64(address, value.evalF64(context))
            else -> throw UnsupportedOperationException()
        }
        return Unit
    }

    override fun children() = listOf(target, value)

    override fun reconstruct(newChildren: List<Node>) = Store(target, value)

    override fun toString(writer: CodeWriter) {
        stringifyChildren(writer, "Store(", ", ", ")")
    }

    override fun toWasm(writer: WasmWriter) {
        when(value.returnType) {
            Bool, I32 -> writer.write(WasmOpcode.I32_STORE)
            I64 -> writer.write(WasmOpcode.I64_STORE)
            F64 -> writer.write(WasmOpcode.F64_STORE)
        }
    }

    override val returnType: Type
        get() = Void

}
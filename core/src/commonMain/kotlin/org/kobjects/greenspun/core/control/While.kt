package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.module.ModuleWriter

class While(
    val condition: Node,
    val body: Node
): Node() {
    override fun eval(context: LocalRuntimeContext): Any {
        while (condition.evalBool(context)) {
            val result = body.eval(context)
            if (result is FlowSignal) {
                when (result.kind) {
                    FlowSignal.Kind.BREAK -> break
                    FlowSignal.Kind.CONTINUE -> continue
                    FlowSignal.Kind.RETURN -> return result
                }
            }
        }
        return Unit
    }

    override fun children() = listOf(condition, body)

    override fun reconstruct(newChildren: List<Node>) =
        While(newChildren[0], newChildren[1])

    override val returnType: Type
        get() = Void

    override fun toString(writer: CodeWriter) {
        writer.write("While(", condition, ", ", body, ")")
    }

    override fun toWasm(writer: ModuleWriter) {
        writer.write(WasmOpcode.BLOCK)
        Void.toWasm(writer)
        writer.write(WasmOpcode.LOOP)
        Void.toWasm(writer)
        condition.toWasm(writer)
        writer.write(WasmOpcode.I32_EQZ)
        writer.write(WasmOpcode.BR_IF)
        writer.writeU32(1)  // Exit block
        body.toWasm(writer)
        writer.write(WasmOpcode.END)
        writer.write(WasmOpcode.END)
    }
}
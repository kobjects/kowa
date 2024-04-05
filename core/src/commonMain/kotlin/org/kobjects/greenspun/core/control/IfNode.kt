package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter

class IfNode(
    val condition: Node,
    val then: Node,
    val otherwise: Node
) : Node() {

    init {

            require( otherwise.returnType == then.returnType) {
                "'then' (${then.returnType}) and 'else' (${otherwise.returnType}) types must match."
            }

    }

    override fun eval(context: LocalRuntimeContext): Any {
        try {
            return if (condition.evalBool(context)) then.eval(context) else otherwise.eval(context)
        } catch (signal: BranchSignal) {
            if (signal.label > 0) {
                throw BranchSignal(signal.label - 1)
            }
            if (returnType != Void) {
                throw IllegalStateException("Return type mismatch")
            }
            return Unit
        }
    }

    override fun children() = listOf(condition, then, otherwise)

    override fun reconstruct(newChildren: List<Node>) =
        IfNode(newChildren[0], newChildren[1], newChildren[2])

    override fun toString(writer: CodeWriter) {
        writer.write("If(", condition)
        writer.write(", ", then, ", ", otherwise, ")")
    }

    override fun toWasm(writer: WasmWriter) {
        condition.toWasm(writer)
        writer.write(WasmOpcode.IF)
        returnType.toWasm(writer)
        then.toWasm(writer)
        writer.write(WasmOpcode.ELSE)
        otherwise.toWasm(writer)
        writer.write(WasmOpcode.END)
    }

    override val returnType: Type
        get() = then.returnType

}
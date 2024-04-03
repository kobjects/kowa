package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.type.Void
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.control.Sequence.Companion.stringifyAsSequence
import org.kobjects.greenspun.core.module.ModuleWriter

class If(
    val condition: Node,
    val then: Node,
    val otherwise: Node?
) : Node() {

    init {
        if (otherwise != null) {
            require( otherwise.returnType == then.returnType) {
                "'then' (${then.returnType}) and 'else' (${otherwise.returnType}) types must match."
            }
        }
    }

    override fun eval(context: LocalRuntimeContext): Any {
        try {
            return if (condition.evalBool(context)) then.eval(context) else otherwise?.eval(context) ?: Unit
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

    override fun children() = if (otherwise == null) listOf(condition, then) else listOf(condition, then, otherwise)

    override fun reconstruct(newChildren: List<Node>) =
        If(newChildren[0], newChildren[1], if (newChildren.size > 2) newChildren[2] else null)

    override fun toString(writer: CodeWriter) {
        writer.write("If(", condition)

        if (otherwise == null || condition is Sequence || otherwise is Sequence) {
            writer.write(") {")
            val inner = writer.indented()
            inner.newLine()
            then.stringifyAsSequence(writer)
            if (otherwise != null) {
                writer.newLine()
                writer.write("}.Else {")
                inner.newLine()
                otherwise.stringifyAsSequence(writer)
            }
            writer.newLine()
            writer.write("}")
        } else {
            writer.write(", ", then, ", ", otherwise, ")")
        }
    }

    override fun toWasm(writer: ModuleWriter) {
        condition.toWasm(writer)
        writer.write(WasmOpcode.IF)
        returnType.toWasm(writer)
        then.toWasm(writer)
        if (otherwise != null) {
            writer.write(WasmOpcode.ELSE)
            otherwise.toWasm(writer)
        }
        writer.write(WasmOpcode.END)
    }

    override val returnType: Type
        get() = if (otherwise == null) Void else then.returnType

}
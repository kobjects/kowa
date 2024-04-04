package org.kobjects.greenspun.core.control

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.module.ModuleWriter
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.type.Void

class ReturnNode(val value: Node) : Node()  {
   override fun eval(context: LocalRuntimeContext): Any {
        throw (ReturnSignal(value.eval(context)))
    }

    override fun children(): List<Node> {
        return listOf(value)
    }

    override fun reconstruct(newChildren: List<Node>): Node {
        return ReturnNode(newChildren[0])
    }

    override fun toString(writer: CodeWriter) {
        writer.write("Return")
        if (value != Void.None) {
            writer.write(" ")
            value.toString(writer)
        }
    }

    override fun toWasm(writer: ModuleWriter) {
        if (value != Void.None) {
            value.toWasm(writer)
        }
        writer.write(WasmOpcode.RETURN)
    }

    override val returnType: Type
        get() = value.returnType
}
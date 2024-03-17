package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.tree.AbstractLeafNode
import org.kobjects.greenspun.core.tree.CodeWriter
import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.module.ModuleWriter

object Void : Type {
    override fun createConstant(value: Any): Node = None
    override fun toWasm(writer: WasmWriter) {
        writer.writeByte(WasmType.VOID.code)
    }

    object None : AbstractLeafNode() {
        override fun eval(context: LocalRuntimeContext) = Unit

        override fun toString(writer: CodeWriter) {
            writer.write("None")
        }

        override val returnType: Type
            get() = Void

        override fun toWasm(writer: ModuleWriter) {
        }
    }

    override fun toString() = "Void"
}
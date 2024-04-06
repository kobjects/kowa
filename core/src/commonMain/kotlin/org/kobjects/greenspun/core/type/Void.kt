package org.kobjects.greenspun.core.type

import org.kobjects.greenspun.core.func.LocalRuntimeContext
import org.kobjects.greenspun.core.expression.AbstractLeafNode
import org.kobjects.greenspun.core.expression.CodeWriter
import org.kobjects.greenspun.core.expression.Node
import org.kobjects.greenspun.core.binary.WasmType
import org.kobjects.greenspun.core.binary.WasmWriter

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

        override fun toWasm(writer: WasmWriter) {
        }
    }

    override fun toString() = "Void"
}
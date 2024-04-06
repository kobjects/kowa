package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.control.SequenceBuilder
import org.kobjects.greenspun.core.module.ModuleBuilder
import org.kobjects.greenspun.core.expression.Node
import org.kobjects.greenspun.core.type.Void

class FuncBuilder(
    moduleBuilder: ModuleBuilder,
    val returnType: Type
) : SequenceBuilder(moduleBuilder, mutableListOf(), WasmWriter()) {

    internal var paramCount = 0

    fun Param(type: Type): LocalReference {

        if (paramCount != variables.size) {
            throw IllegalStateException("Parameters can't be declared after local variables.")
        }

        if (wasmWriter.size != 0) {
            throw IllegalStateException("Parameters can't be declared after statements.")
        }

        val variable = LocalReference(variables.size, false, type)
        variables.add(type)

        paramCount++

        return variable
    }

    fun Return(value: Any = Void.None) {
        val node = Node.of(value)
        require(node.returnType == returnType) {
            "Return value type (${node.returnType}) does not match function return type ($returnType)."
        }
        if (node.returnType != Void) {
            node.toWasm(wasmWriter)
        }
        wasmWriter.write(WasmOpcode.RETURN)
    }

    internal fun build() = FuncImpl(
        index = moduleBuilder.funcs.size,
        type = moduleBuilder.getFuncType(returnType, variables.subList(0, paramCount)),
        locals = variables.subList(paramCount, variables.size),
        body = wasmWriter.toWasm()
    )
}
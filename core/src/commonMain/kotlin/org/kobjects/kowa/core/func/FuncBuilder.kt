package org.kobjects.kowa.core.func

import org.kobjects.kowa.binary.WasmOpcode
import org.kobjects.kowa.binary.WasmWriter
import org.kobjects.kowa.core.type.Type
import org.kobjects.kowa.core.module.ModuleBuilder
import org.kobjects.kowa.core.expr.Expr

class FuncBuilder(
    moduleBuilder: ModuleBuilder,
    val returnType: List<Type>
) : BodyBuilder(moduleBuilder, BlockType.FUNCTION, null, mutableListOf(), WasmWriter(), returnType) {

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

    fun Return(vararg value: Any) {
        val children = value.map { Expr.of(it) }
/*        require(children.map {it.returnType } == returnType) {
            "Return value type (${node.returnType}) does not match function return type ($returnType)."
        }*/
        for (child in children) {
            child.toWasm(wasmWriter)
        }
        wasmWriter.writeOpcode(WasmOpcode.RETURN)
        unreachableCodePosition = wasmWriter.size
    }

    internal fun build(): FuncImpl {
        close()
        require(wasmWriter.openBlocks.isEmpty()) {
            "Unexpected open block"
        }
        wasmWriter.writeOpcode(WasmOpcode.END)
        return FuncImpl(
            index = moduleBuilder.funcs.size,
            type = moduleBuilder.getFuncType(returnType, variables.subList(0, paramCount)),
            locals = variables.subList(paramCount, variables.size),
            body = wasmWriter.toWasm())
    }
}
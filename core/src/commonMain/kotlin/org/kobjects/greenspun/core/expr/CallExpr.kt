package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.FuncInterface


class CallExpr(
    val callable: FuncInterface,
    vararg parameters: Expr
) : Expr(*parameters) {

    init {
        require(callable.type.parameterTypes.size == parameters.size) {
            "${callable.type.parameterTypes.size} parameters expected, but got ${parameters.size}"}

        /*
        for (i in parameters.indices) {
            val expectedType = callable.type.parameterTypes[i]
            val actualType = parameters[i].returnType
            require(expectedType == actualType) {
                "Type mismatch for parameter $i; expected type: $expectedType; actual type: $actualType "}
        }

         */
    }


    override fun toString(writer: CodeWriter) {
        writer.write("func${callable.index}(")
        for (i in children.indices) {
            if (i > 0) {
                writer.write(", ")
            }
            children[i].toString(writer)
        }
        writer.write(")")
    }

    override fun toWasm(writer: WasmWriter) {
        super.toWasm(writer)
        writer.writeOpcode(WasmOpcode.CALL)
        writer.writeU32(callable.index)
    }

    override val returnType: List<Type>
        get() = callable.type.returnType
}
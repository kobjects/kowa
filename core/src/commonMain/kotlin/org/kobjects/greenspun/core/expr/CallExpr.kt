package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.Type
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.binary.WasmWriter
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.LocalRuntimeContext


class CallExpr(
    val callable: FuncInterface,
    vararg val parameters: Expr
) : Expr() {


    init {
        require(callable.type.parameterTypes.size == parameters.size) {
            "${callable.type.parameterTypes.size} parameters expected, but got ${parameters.size}"}

        for (i in parameters.indices) {
            val expectedType = callable.type.parameterTypes[i]
            val actualType = parameters[i].returnType
            require(expectedType == actualType) {
                "Type mismatch for parameter $i; expected type: $expectedType; actual type: $actualType "}
        }
    }

    override fun eval(context: LocalRuntimeContext): Any {
        return callable.call(context, *parameters)
    }

    override fun children(): List<Expr> = parameters.toList()

    override fun reconstruct(newChildren: List<Expr>) =
        CallExpr(callable, *newChildren.toTypedArray())

    override fun toString(writer: CodeWriter) {
        writer.write("func${callable.index}(")
        for (i in parameters.indices) {
            if (i > 0) {
                writer.write(", ")
            }
            parameters[i].toString(writer)
        }
        writer.write(")")
    }

    override fun toWasm(writer: WasmWriter) {
        for (parameter in parameters) {
            parameter.toWasm(writer)
        }
        writer.write(WasmOpcode.CALL)
        writer.writeU32(callable.index)
    }

    override val returnType: Type
        get() = callable.type.returnType
}
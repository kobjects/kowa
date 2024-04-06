package org.kobjects.greenspun.core.instance

import org.kobjects.greenspun.core.binary.Wasm
import org.kobjects.greenspun.core.binary.WasmOpcode
import org.kobjects.greenspun.core.func.LocalRuntimeContext

class WasmInterpreter(
    val wasm: Wasm,
    val localRuntimeContext: LocalRuntimeContext
) {

    val stack = mutableListOf<Any>()
    val blockStack = mutableListOf<Int>()

    fun pop() = stack.removeLast()

    fun popI32() = stack.removeLast() as Int

    fun popI64() = stack.removeLast() as Long

    fun popBool() = popI32() != 0

    fun push(value: Any) =
        stack.add(value)


    fun push(value: Boolean) =
        stack.add(if (value) 1 else 0)


    var ip = 0

    fun run(): Any {
        blockStack.add(0)
        while (ip < wasm.code.size) {
            step()
        }
        return stack.removeLast()
    }

    fun step() {
        val ip0 = ip
        val opcode = WasmOpcode.of(wasm.code[ip++].toInt())

        println("$ip0: $opcode")

        when (opcode) {
            WasmOpcode.NOP -> {}
            WasmOpcode.UNREACHABLE ->
                throw UnsupportedOperationException(opcode.name)
            WasmOpcode.BLOCK -> {
                immediateType()
                blockStack.add(ip0)
            }
            WasmOpcode.LOOP -> {
                immediateType()
                blockStack.add(ip0)
            }
            WasmOpcode.IF -> {
                immediateType()
                if (popBool()) {
                    blockStack.add(ip0)
                } else {
                    val elsePos = wasm.elsePositions[ip0]
                    if (elsePos != null) {
                        blockStack.add(ip0)
                        ip = elsePos
                    } else {
                        ip = wasm.endPositions[ip0]!!
                    }
                }
            }
            WasmOpcode.ELSE ->
                ip = wasm.endPositions[blockStack.removeLast()]!!

            WasmOpcode.END -> blockStack.removeLast()
            WasmOpcode.BR -> br(immediateU32())
            WasmOpcode.BR_IF -> {
                val depth = immediateU32()
                if (popBool()) {
                    br(depth)
                }
            }
            WasmOpcode.BR_TABLE -> throw UnsupportedOperationException()
            WasmOpcode.RETURN -> ip = wasm.code.size
            WasmOpcode.CALL -> {
                val func = localRuntimeContext.instance.module.funcs[immediateU32()]
                val type = func.type
                val parCount = type.parameterTypes.size
                val result = func.call(localRuntimeContext, *stack.subList(stack.size - parCount, stack.size).toTypedArray())
                for (i in 0 until parCount) {
                    pop()
                }
                push(result)
            }
            WasmOpcode.CALL_INDIRECT -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.DROP -> stack.removeLast()
            WasmOpcode.SELECT -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.SELECT_T -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.LOCAL_GET -> push(localRuntimeContext.getLocal(immediateU32()))
            WasmOpcode.LOCAL_SET -> localRuntimeContext.setLocal(immediateU32(), pop())
            WasmOpcode.LOCAL_TEE -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.GLOBAL_GET -> push(localRuntimeContext.instance.getGlobal(immediateU32()))
            WasmOpcode.GLOBAL_SET -> localRuntimeContext.instance.setGlobal(immediateU32(), pop())

            WasmOpcode.TABLE_GET -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.TABLE_SET -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.I32_LOAD -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LOAD -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_LOAD -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F64_LOAD -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_LOAD_8_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_LOAD_8_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_LOAD_16_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_LOAD_16_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LOAD_8_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LOAD_8_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LOAD_16_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LOAD_16_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LOAD_32_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LOAD_32_U -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.I32_STORE -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_STORE -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_STORE -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F64_STORE -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_STORE_8 -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_STORE_16 -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_STORE_8 -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_STORE_16 -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_STORE_32 -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.MEMORY_SIZE -> push(localRuntimeContext.instance.memory.buffer.size / 65536)
            WasmOpcode.MEMORY_GROW -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.I32_CONST -> push(immediateI32())
            WasmOpcode.I64_CONST -> push(immediateI64())
            WasmOpcode.F32_CONST -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F64_CONST -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.I32_EQZ -> push(popI32() == 0)
            WasmOpcode.I32_EQ -> push(popI32() == popI32())
            WasmOpcode.I32_NE -> push(popI32() != popI32())
            WasmOpcode.I32_LT_S -> {
                val c2 = popI32()
                push(popI32() < c2)
            }
            WasmOpcode.I32_LT_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_GT_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_GT_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_LE_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_LE_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I32_GE_S -> {
                val c2 = popI32()
                push(popI32() >= c2)
            }
            WasmOpcode.I32_GE_U -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.I64_EQZ -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_EQ -> push(popI64() == popI64())
            WasmOpcode.I64_NE -> push(popI64() != popI64())
            WasmOpcode.I64_LT_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LT_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_GT_S -> {
                val r = popI64()
                push(popI64() > r)
            }
            WasmOpcode.I64_GT_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LE_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_LE_U -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_GE_S -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.I64_GE_U -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.F32_EQ -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_NE -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_LT -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_GT -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_LE -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_GE -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F64_EQ -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F64_NE -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_LT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_GT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_LE -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_GE -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_CLZ -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_CTZ -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_POPCNT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_ADD -> push(popI32() + popI32())
                WasmOpcode.I32_SUB -> {
                    val c2 = popI32()
                    push(popI32() - c2)
                }
                WasmOpcode.I32_MUL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_DIV_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_DIV_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_REM_S -> {
                    val c2 = popI32()
                    push(popI32() % c2)
                }
                WasmOpcode.I32_REM_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_AND -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_OR -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_XOR -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_SHL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_SHR_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_SHR_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_ROTL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_ROTR -> throw UnsupportedOperationException(opcode.name)

                WasmOpcode.I64_CLZ -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_CTZ -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_POPCNT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_ADD -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_SUB -> {
                    val c2 = popI64()
                    push(popI64() - c2)
                }
                WasmOpcode.I64_MUL -> push(popI64() * popI64())
                WasmOpcode.I64_DIV_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_DIV_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_REM_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_REM_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_AND -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_OR -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_XOR -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_SHL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_SHR_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_SHR_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_ROTL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_ROTR -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_ABS -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_NEG -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_CEIL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_FLOOR -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_TRUNC -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_NEAREST -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_SQRT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_ADD -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_SUB -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_MUL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_DIV -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_MIN -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_MAX -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_COPYSIGN -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_ABS -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_NEG -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_CEIL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_FLOOR -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_TRUNC -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_NEAREST -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_SQRT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_ADD -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_SUB -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_MUL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_DIV -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_MIN -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_MAX -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_COPYSIGN -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_WRAP_I64 -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_TRUNC_F32_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_TRUNC_F32_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_TRUNC_F64_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_TRUNC_F64_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_EXTEND_I32_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_EXTEND_I32_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_TRUNC_F32_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_TRUNC_F32_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_TRUNC_F64_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_TRUNC_F64_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_CONVERT_I32_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_CONVERT_I32_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_CONVERT_I64_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_CONVERT_I64_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_DEMOTE_F64 -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_CONVERT_I32_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_CONVERT_I32_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_CONVERT_I64_S -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_CONVERT_I64_U -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_PROMOTE_F32 -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I32_REINTERPRET_F32 -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.I64_REINTERPRET_F64 -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F32_REINTERPRET_I32 -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.F64_REINTERPRET_I64 -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.REF_NULL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.REF_IS_NULL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.REF_FUNC -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.MEMORY_INIT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.DATA_DROP -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.MEMORY_COPY -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.MEMORY_FILL -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.TABLE_INIT -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.TABLE_DROP -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.TABLE_COPY -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.TABLE_GROW -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.TABLE_SIZE -> throw UnsupportedOperationException(opcode.name)
                WasmOpcode.TABLE_FILL -> throw UnsupportedOperationException(opcode.name)

        }


    }

    fun immediateType() {
        immediateU32()
    }

    fun br(depth: Int) {
        for (i in 0 until depth) {
            blockStack.removeLast()
        }
        val blockStart = blockStack.removeLast()
        ip =
            if (wasm.code[blockStart].toInt() == WasmOpcode.LOOP.code) blockStart else wasm.endPositions[blockStart] ?:
            throw IllegalStateException("Can't find end position for $blockStart in ${wasm.endPositions}")

    }


    fun immediateI32(): Int {
        var result = 0
        var shift = 0
        while(true) {
            val byte = wasm.code[ip++]
            result = result or ((byte.toInt() and 127) shl shift)
            shift += 7
            if (byte >= 0) {
                if (shift < 32 && ((byte.toInt() and 0x40) != 0)) {
                    result = result or (0.inv() shl shift)
                }
                return result
            }
        }
    }

    fun immediateI64(): Long {
        var result = 0L
        var shift = 0
        while(true) {
            val byte = wasm.code[ip++]
            result = result or ((byte.toLong() and 127L) shl shift)
            shift += 7
            if (byte >= 0) {
                if (shift < 64 && ((byte.toInt() and 0x40) != 0)) {
                    result = result or (0L.inv() shl shift)
                }
                return result
            }
        }
    }

    fun immediateU32(): Int {
        var result = 0
        var shift = 0
        while (true) {
            val byte = wasm.code[ip++]
            result = result or ((byte.toInt() and 127) shl shift)
            if (byte >= 0) {
                return result
            }
            shift += 7
        }
    }

}
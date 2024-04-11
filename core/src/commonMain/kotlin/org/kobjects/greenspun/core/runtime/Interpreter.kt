package org.kobjects.greenspun.core.runtime

import org.kobjects.greenspun.core.binary.*
import org.kobjects.greenspun.core.func.FuncInterface
import org.kobjects.greenspun.core.func.LocalRuntimeContext
import kotlin.math.*

class Interpreter(
    val wasm: Wasm,
    val localRuntimeContext: LocalRuntimeContext,
) {

    val stack = Stack()
    val blockStack = mutableListOf<Int>()
    var ip = 0


    fun run(): Any {
        blockStack.add(0)
        while (ip < wasm.code.size) {
            step()
        }
        return stack.popAny()
    }

    fun step() {
        val ip0 = ip
        val opcode = WasmOpcode.of(wasm.code[ip++].toInt())

        println("$ip0: $opcode")

        when (opcode) {
            WasmOpcode.NOP -> {}
            WasmOpcode.UNREACHABLE ->
                throw IllegalStateException("Unreachable")
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
                if (stack.popBool()) {
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
                if (stack.popBool()) {
                    br(depth)
                }
            }
            WasmOpcode.BR_TABLE -> throw UnsupportedOperationException()
            WasmOpcode.RETURN -> ip = wasm.code.size
            WasmOpcode.CALL -> call(localRuntimeContext.instance.module.funcs[immediateU32()])
            WasmOpcode.CALL_INDIRECT -> {
                val i = stack.popI32()
                val typeIdx = immediateU32()
                val tableIdx = immediateU32()
                val table = localRuntimeContext.instance.tables[tableIdx]
                val f = table.elements[i] as FuncInterface
                require(typeIdx == f.type.index) {
                    "Indirect call type mismatch. Expected type is ${localRuntimeContext.instance.module.types[typeIdx]} index $typeIdx; actual: ${f.type} index ${f.type.index}"
                }
                return call(f)
            }
            WasmOpcode.DROP -> stack.popAny()
            WasmOpcode.SELECT -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.SELECT_T -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.LOCAL_GET -> stack.pushAny(localRuntimeContext.getLocal(immediateU32()))
            WasmOpcode.LOCAL_SET -> localRuntimeContext.setLocal(immediateU32(), stack.popAny())
            WasmOpcode.LOCAL_TEE -> localRuntimeContext.setLocal(immediateU32(), stack.peekAny())

            WasmOpcode.GLOBAL_GET -> stack.pushAny(localRuntimeContext.instance.getGlobal(immediateU32()))
            WasmOpcode.GLOBAL_SET -> localRuntimeContext.instance.setGlobal(immediateU32(), stack.popAny())

            WasmOpcode.TABLE_GET -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.TABLE_SET -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.I32_LOAD -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushI32(localRuntimeContext.instance.memory.buffer.loadI32(offset + stack.popI32()))
            }
            WasmOpcode.I64_LOAD -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushI64(localRuntimeContext.instance.memory.buffer.loadI64(offset + stack.popI32()))
            }
            WasmOpcode.F32_LOAD -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushF32(localRuntimeContext.instance.memory.buffer.loadF32(offset +stack.popI32()))
            }
            WasmOpcode.F64_LOAD -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushF64(localRuntimeContext.instance.memory.buffer.loadF64(offset +stack.popI32()))
            }
            WasmOpcode.I32_LOAD_8_S -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushI32(localRuntimeContext.instance.memory.buffer[offset + stack.popI32()].toInt())
            }
            WasmOpcode.I32_LOAD_8_U -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushU32(localRuntimeContext.instance.memory.buffer[offset + stack.popI32()].toUByte().toUInt())
            }
            WasmOpcode.I32_LOAD_16_S -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushI32(localRuntimeContext.instance.memory.buffer.loadI16(offset + stack.popI32()).toInt())
            }
            WasmOpcode.I32_LOAD_16_U -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushU32(localRuntimeContext.instance.memory.buffer.loadU16(offset + stack.popI32()).toUInt())
            }
            WasmOpcode.I64_LOAD_8_S -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushI64(localRuntimeContext.instance.memory.buffer[offset + stack.popI32()].toLong())
            }
            WasmOpcode.I64_LOAD_8_U -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushU64(localRuntimeContext.instance.memory.buffer[offset + stack.popI32()].toULong())
            }
            WasmOpcode.I64_LOAD_16_S -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushI64(localRuntimeContext.instance.memory.buffer.loadI16(offset + stack.popI32()).toLong())
            }
            WasmOpcode.I64_LOAD_16_U -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushU64(localRuntimeContext.instance.memory.buffer.loadU16(offset + stack.popI32()).toULong())
            }
            WasmOpcode.I64_LOAD_32_S -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushI64(localRuntimeContext.instance.memory.buffer.loadI32(offset + stack.popI32()).toLong())
            }
            WasmOpcode.I64_LOAD_32_U -> {
                val align = immediateU32()
                val offset = immediateU32()
                stack.pushU64(localRuntimeContext.instance.memory.buffer.loadI32(offset + stack.popI32()).toULong())
            }

            WasmOpcode.I32_STORE -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popI32()
                localRuntimeContext.instance.memory.buffer.storeI32(offset + stack.popI32(), value)
            }
            WasmOpcode.I64_STORE -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popI64()
                localRuntimeContext.instance.memory.buffer.storeI64(offset + stack.popI32(), value)
            }
            WasmOpcode.F32_STORE -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popF32()
                localRuntimeContext.instance.memory.buffer.storeF32(offset + stack.popI32(), value)
            }
            WasmOpcode.F64_STORE -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popF64()
                localRuntimeContext.instance.memory.buffer.storeF64(offset + stack.popI32(), value)
            }
            WasmOpcode.I32_STORE_8 -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popI32().toByte()
                localRuntimeContext.instance.memory.buffer[offset + stack.popI32()] = value
            }
            WasmOpcode.I32_STORE_16 -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popI32().toShort()
                localRuntimeContext.instance.memory.buffer.storeI16(offset + stack.popI32(), value)
            }
            WasmOpcode.I64_STORE_8 -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popI64().toByte()
                localRuntimeContext.instance.memory.buffer[offset + stack.popI32()] = value
            }
            WasmOpcode.I64_STORE_16 -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popI64().toShort()
                localRuntimeContext.instance.memory.buffer.storeI16(offset + stack.popI32(), value)
            }
            WasmOpcode.I64_STORE_32 -> {
                val align = immediateU32()
                val offset = immediateU32()
                val value = stack.popI64().toInt()
                localRuntimeContext.instance.memory.buffer.storeI32(offset + stack.popI32(), value)
            }

            WasmOpcode.MEMORY_SIZE -> stack.pushI32(localRuntimeContext.instance.memory.buffer.size / 65536)
            WasmOpcode.MEMORY_GROW -> throw UnsupportedOperationException(opcode.name)

            WasmOpcode.I32_CONST -> stack.pushI32(immediateI32())
            WasmOpcode.I64_CONST -> stack.pushI64(immediateI64())
            WasmOpcode.F32_CONST -> stack.pushF32(Float.fromBits(immediateU32()))
            WasmOpcode.F64_CONST -> stack.pushF64(Double.fromBits(immediateU64()))

            WasmOpcode.I32_EQZ -> stack.pushBool(stack.popI32() == 0)
            WasmOpcode.I32_EQ -> {
                val c2 = stack.popI32()
                val c1 = stack.popI32()
                val result = c1 == c2
                if (!result) {
                    println("******* $c1 != $c2")
                }
                stack.pushBool(result)
            }
            WasmOpcode.I32_NE -> stack.pushBool(stack.popI32() != stack.popI32())
            WasmOpcode.I32_LT_S -> stack.replaceBool(2, stack.peekI32(1) < stack.peekI32(0))
            WasmOpcode.I32_LT_U -> stack.replaceBool(2, stack.peekU32(1) < stack.peekU32(0))
            WasmOpcode.I32_GT_S -> stack.replaceBool(2, stack.peekI32(1) > stack.peekI32(0))
            WasmOpcode.I32_GT_U -> stack.replaceBool(2, stack.peekU32(1) > stack.peekU32(0))
            WasmOpcode.I32_LE_S -> stack.replaceBool(2, stack.peekI32(1) <= stack.peekI32(0))
            WasmOpcode.I32_LE_U -> stack.replaceBool(2, stack.peekU32(1) <= stack.peekU32(0))
            WasmOpcode.I32_GE_S -> stack.replaceBool(2, stack.peekI32(1) >= stack.peekI32(0))
            WasmOpcode.I32_GE_U -> stack.replaceBool(2, stack.peekU32(1) >= stack.peekU32(0))

            WasmOpcode.I64_EQZ -> stack.pushBool(stack.popI64() == 0L)
            WasmOpcode.I64_EQ -> stack.pushBool(stack.popI64() == stack.popI64())
            WasmOpcode.I64_NE -> stack.pushBool(stack.popI64() != stack.popI64())
            WasmOpcode.I64_LT_S -> stack.replaceBool(2, stack.peekI64(1) < stack.peekI64(0))
            WasmOpcode.I64_LT_U -> stack.replaceBool(2, stack.peekU64(1) < stack.peekU64(0))
            WasmOpcode.I64_GT_S -> stack.replaceBool(2, stack.peekI64(1) > stack.peekI64(0))
            WasmOpcode.I64_GT_U -> stack.replaceBool(2, stack.peekU64(1) > stack.peekU64(0))
            WasmOpcode.I64_LE_S -> stack.replaceBool(2, stack.peekI64(1) <= stack.peekI64(0))
            WasmOpcode.I64_LE_U -> stack.replaceBool(2, stack.peekU64(1) <= stack.peekU64(0))
            WasmOpcode.I64_GE_S -> stack.replaceBool(2, stack.peekI64(1) >= stack.peekI64(0))
            WasmOpcode.I64_GE_U -> stack.replaceBool(2, stack.peekU64(1) >= stack.peekU64(0))

            WasmOpcode.F32_EQ -> stack.pushBool(stack.popF32() == stack.popF32())
            WasmOpcode.F32_NE -> stack.pushBool(stack.popF32() != stack.popF32())
            WasmOpcode.F32_LT -> stack.replaceBool(2, stack.peekF32(1) < stack.peekF32(0))
            WasmOpcode.F32_GT -> stack.replaceBool(2, stack.peekF32(1) > stack.peekF32(0))
            WasmOpcode.F32_LE -> stack.replaceBool(2, stack.peekF32(1) <= stack.peekF32(0))
            WasmOpcode.F32_GE -> stack.replaceBool(2, stack.peekF32(1) >= stack.peekF32(0))
            WasmOpcode.F64_EQ -> stack.pushBool(stack.popF64() == stack.popF64())
            WasmOpcode.F64_NE -> stack.pushBool(stack.popF64() != stack.popF64())
            WasmOpcode.F64_LT -> stack.replaceBool(2, stack.peekF64(1) < stack.peekF64(0))
            WasmOpcode.F64_GT -> stack.replaceBool(2, stack.peekF64(1) > stack.peekF64(0))
            WasmOpcode.F64_LE -> stack.replaceBool(2, stack.peekF64(1) <= stack.peekF64(0))
            WasmOpcode.F64_GE -> stack.replaceBool(2, stack.peekF64(1) >= stack.peekF64(0))

            WasmOpcode.I32_CLZ -> stack.pushI32(stack.popI32().countLeadingZeroBits())
            WasmOpcode.I32_CTZ ->  stack.pushI32(stack.popI32().countTrailingZeroBits())
            WasmOpcode.I32_POPCNT -> stack.pushI32(stack.popI32().countOneBits())
            WasmOpcode.I32_ADD -> stack.pushI32(stack.popI32() + stack.popI32())
            WasmOpcode.I32_SUB -> stack.replaceI32(2, stack.peekI32(1) - stack.peekI32(0))
            WasmOpcode.I32_MUL -> stack.pushI32(stack.popI32() * stack.popI32())
            WasmOpcode.I32_DIV_S -> stack.replaceI32(2, stack.peekI32(1) / stack.peekI32(0))
            WasmOpcode.I32_DIV_U -> stack.replaceU32(2, stack.peekU32(1) / stack.peekU32(0))
            WasmOpcode.I32_REM_S -> stack.replaceI32(2, stack.peekI32(1) % stack.peekI32(0))
            WasmOpcode.I32_REM_U -> stack.replaceU32(2, stack.peekU32(1) % stack.peekU32(0))
            WasmOpcode.I32_AND -> stack.pushI32(stack.popI32() and stack.popI32())
            WasmOpcode.I32_OR -> stack.pushI32(stack.popI32() or stack.popI32())
            WasmOpcode.I32_XOR -> stack.pushI32(stack.popI32() xor stack.popI32())
            WasmOpcode.I32_SHL -> stack.replaceI32(2, stack.peekI32(1) shl stack.peekI32(0))
            WasmOpcode.I32_SHR_S -> stack.replaceI32(2, stack.peekI32(1) shr stack.peekI32(0))
            WasmOpcode.I32_SHR_U -> stack.replaceI32(2, stack.peekI32(1) ushr stack.peekI32(0))
            WasmOpcode.I32_ROTL -> stack.replaceI32(2, stack.peekI32(1).rotateLeft(stack.peekI32(0)))
            WasmOpcode.I32_ROTR -> stack.replaceI32(2, stack.peekI32(1).rotateRight(stack.peekI32(0)))

            WasmOpcode.I64_CLZ -> stack.pushI64(stack.popI64().countLeadingZeroBits().toLong())
            WasmOpcode.I64_CTZ ->  stack.pushI64(stack.popI64().countTrailingZeroBits().toLong())
            WasmOpcode.I64_POPCNT -> stack.pushI64(stack.popI64().countOneBits().toLong())
            WasmOpcode.I64_ADD -> stack.pushI64(stack.popI64() + stack.popI64())
            WasmOpcode.I64_SUB -> stack.replaceI64(2, stack.peekI64(1) - stack.peekI64(0))
            WasmOpcode.I64_MUL -> stack.pushI64(stack.popI64() * stack.popI64())
            WasmOpcode.I64_DIV_S -> stack.pushI64(stack.popI64() * stack.popI64())
            WasmOpcode.I64_DIV_U -> stack.replaceU32(2, stack.peekU32(1) / stack.peekU32(0))
            WasmOpcode.I64_REM_S -> stack.replaceI64(2, stack.peekI64(1) % stack.peekI64(0))
            WasmOpcode.I64_REM_U -> stack.replaceU32(2, stack.peekU32(1) % stack.peekU32(0))
            WasmOpcode.I64_AND -> stack.pushI64(stack.popI64() and stack.popI64())
            WasmOpcode.I64_OR -> stack.pushI64(stack.popI64() or stack.popI64())
            WasmOpcode.I64_XOR -> stack.pushI64(stack.popI64() xor stack.popI64())
            WasmOpcode.I64_SHL -> stack.replaceI64(2, stack.peekI64(1) shl stack.peekI64(0).toInt())
            WasmOpcode.I64_SHR_S -> stack.replaceI64(2, stack.peekI64(1) shr stack.peekI64(0).toInt())
            WasmOpcode.I64_SHR_U -> stack.replaceI64(2, stack.peekI64(1) ushr stack.peekI64(0).toInt())
            WasmOpcode.I64_ROTL -> stack.replaceI64(2, stack.peekI64(1).rotateLeft(stack.peekI64().toInt()))
            WasmOpcode.I64_ROTR -> stack.replaceI64(2, stack.peekI64(1).rotateRight(stack.peekI64().toInt()))

            WasmOpcode.F32_ABS -> stack.pushF32(stack.popF32().absoluteValue)
            WasmOpcode.F32_NEG -> stack.pushF32(-stack.popF32())
            WasmOpcode.F32_CEIL -> stack.pushF32(ceil(stack.popF32()))
            WasmOpcode.F32_FLOOR -> stack.pushF32(floor(stack.popF32()))
            WasmOpcode.F32_TRUNC -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_NEAREST -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F32_SQRT -> stack.pushF32(sqrt(stack.popF32()))
            WasmOpcode.F32_ADD -> stack.pushF32(stack.popF32() + stack.popF32())
            WasmOpcode.F32_SUB -> stack.replaceF32(2, stack.peekF32(1) - stack.peekF32(0))
            WasmOpcode.F32_MUL -> stack.pushF32(stack.popF32() * stack.popF32())
            WasmOpcode.F32_DIV -> stack.replaceF32(2, stack.peekF32(1) / stack.peekF32(0))
            WasmOpcode.F32_MIN -> stack.pushF32(min(stack.popF32(), stack.popF32()))
            WasmOpcode.F32_MAX -> stack.pushF32(max(stack.popF32(), stack.popF32()))
            WasmOpcode.F32_COPYSIGN -> {
                val r = stack.popF32()
                val l = stack.popF32()
                stack.pushF32(if (l.sign == r.sign) l else -l)
            }
            WasmOpcode.F64_ABS -> stack.pushF64(stack.popF64().absoluteValue)
            WasmOpcode.F64_NEG -> stack.pushF64(-stack.popF64())
            WasmOpcode.F64_CEIL -> stack.pushF64(ceil(stack.popF64()))
            WasmOpcode.F64_FLOOR -> stack.pushF64(floor(stack.popF64()))
            WasmOpcode.F64_TRUNC -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F64_NEAREST -> throw UnsupportedOperationException(opcode.name)
            WasmOpcode.F64_SQRT -> stack.pushF64(sqrt(stack.popF64()))
            WasmOpcode.F64_ADD -> stack.pushF64(stack.popF64() + stack.popF64())
            WasmOpcode.F64_SUB -> stack.replaceF64(2, stack.peekF64(1) - stack.peekF64(0))
            WasmOpcode.F64_MUL -> stack.pushF64(stack.popF64() * stack.popF64())
            WasmOpcode.F64_DIV -> stack.replaceF64(2, stack.peekF64(1) / stack.peekF64(0))
            WasmOpcode.F64_MIN -> stack.pushF64(min(stack.popF64(), stack.popF64()))
            WasmOpcode.F64_MAX -> stack.pushF64(max(stack.popF64(), stack.popF64()))
            WasmOpcode.F64_COPYSIGN -> {
                val r = stack.popF64()
                val l = stack.popF64()
                stack.pushF64(if (l.sign == r.sign) l else -l)
            }

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
            WasmOpcode.F32_CONVERT_I32_S -> stack.pushF32(stack.popI32().toFloat())
            WasmOpcode.F32_CONVERT_I32_U -> stack.pushF32(stack.popU32().toFloat())
            WasmOpcode.F32_CONVERT_I64_S -> stack.pushF32(stack.popI64().toFloat())
            WasmOpcode.F32_CONVERT_I64_U -> stack.pushF32(stack.popU64().toFloat())
            WasmOpcode.F32_DEMOTE_F64 -> stack.pushF32(stack.popF64().toFloat())
            WasmOpcode.F64_CONVERT_I32_S -> stack.pushF64(stack.popI32().toDouble())
            WasmOpcode.F64_CONVERT_I32_U -> stack.pushF64(stack.popU32().toDouble())
            WasmOpcode.F64_CONVERT_I64_S -> stack.pushF64(stack.popI64().toDouble())
            WasmOpcode.F64_CONVERT_I64_U -> stack.pushF64(stack.popU64().toDouble())
            WasmOpcode.F64_PROMOTE_F32 -> stack.pushF64(stack.popF32().toDouble())
            WasmOpcode.I32_REINTERPRET_F32 -> stack.pushI32(stack.popF32().toBits())
            WasmOpcode.I64_REINTERPRET_F64 -> stack.pushI64(stack.popF64().toBits())
            WasmOpcode.F32_REINTERPRET_I32 -> stack.pushF32(Float.fromBits(stack.popI32()))
            WasmOpcode.F64_REINTERPRET_I64 -> stack.pushF64(Double.fromBits(stack.popI64()))

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


    fun call(f: FuncInterface) {
        val type = f.type
        val parCount = type.parameterTypes.size
        val result = f.call(localRuntimeContext, *stack.stack.subList(stack.size - parCount, stack.size).toTypedArray())
        stack.replaceAny(type.parameterTypes.size, result)
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

    fun immediateU64(): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val byte = wasm.code[ip++]
            result = result or ((byte.toLong() and 127L) shl shift)
            if (byte >= 0) {
                return result
            }
            shift += 7
        }
    }
}
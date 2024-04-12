package org.kobjects.greenspun

import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.F64
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.I64
import kotlin.test.*

class MemoryTest {

    @Test
    fun testSimpleMemory() {
        Module { Memory(0) }
        Module { Memory(1) }
        Module { Memory(0, 0) }
        Module { Memory(0, 1) }
        Module { Memory(1, 256) }
        Module { Memory(0, 65536) }
    }

    @Test
    fun testMultipleMemories() {
        assertFailsWith(
            exceptionClass = IllegalStateException::class,
            message = "multiple memories"
        ) {
            Module {
                Memory(0)
                Memory(0)
            }
        }

        assertFailsWith(
            exceptionClass = IllegalStateException::class,
            message = "multiple memories"
        ) {
            Module {
                ImportMemory("spectest", "memory", 0)
                Memory(0)
            }
        }
    }

    @Test
    fun testMemorySize() {
        assertEquals(
            0,
            Module {
                val memory = Memory(0)
                memory.data("")
                Export("memsize", Func(I32) { Return(memory.size) })
            }.instantiate().invoke("memsize")
        )

        assertEquals(
            1,
            Module {
                val memory = Memory(1)
                memory.data("x")
                Export("memsize", Func(I32) { Return(memory.size) })
            }.instantiate().invoke("memsize")
        )
    }

    @Test
    fun testMemory() {
        val module = Module {
            val mem = Memory(1)
            mem.data(0, "ABC\u00a7D")
            mem.data(20, "WASM")

            val mem8 = mem.u8i32

            Export("data", Func(Bool) {
                Return((mem8[0] Eq 65)
                        And (mem8[3] Eq 0x0c2)
                        And (mem8[4] Eq 0x0a7)
                        And (mem8[6] Eq 0)
                        And (mem8[19] Eq 0)
                        And (mem8[20] Eq 87)
                        And (mem8[23] Eq 77)
                        And (mem8[24] Eq 0)
                        And (mem8[1023] Eq 0))
            })

            Export("i32_load_8_s", Func(I32) {
                val i = Param(I32)
                mem.s8i32[8] = i
                Return(mem.s8i32[8])
            })
            Export("i32_load_8_u", Func(I32) {
                val i = Param(I32)
                mem.u8i32[8] = i
                Return(mem.u8i32[8])
            })
            Export("i32_load_16_s", Func(I32) {
                val i = Param(I32)
                mem.s16i32[8] = i
                Return(mem.s16i32[8])
            })
            Export("i32_load_16_u", Func(I32) {
                val i = Param(I32)
                mem.u16i32[8] = i
                Return(mem.u16i32[8])
            })
            Export("i64_load_8_s", Func(I64) {
                val i = Param(I64)
                mem.s8i64[8] = i
                Return(mem.s8i64[8])
            })
            Export("i64_load_8_u", Func(I64) {
                val i = Param(I64)
                mem.u8i64[8] = i
                Return(mem.u8i64[8])
            })
            Export("i64_load_16_s", Func(I64) {
                val i = Param(I64)
                mem.s16i64[8] = i
                Return(mem.s16i64[8])
            })
            Export("i64_load_16_u", Func(I64) {
                val i = Param(I64)
                mem.u16i64[8] = i
                Return(mem.u16i64[8])
            })
            Export("i64_load_32_s", Func(I64) {
                val i = Param(I64)
                mem.s32i64[8] = i
                Return(mem.s32i64[8])
            })
            Export("i64_load_32_u", Func(I64) {
                val i = Param(I64)
                mem.u32i64[8] = i
                Return(mem.u32i64[8])
            })
        }

        val instance = module.instantiate()

        assertEquals(1, instance.invoke("data"))

        assertEquals(-1, instance.invoke("i32_load_8_s", -1))
        assertEquals(255, instance.invoke("i32_load_8_u", -1))
        assertEquals(-1, instance.invoke("i32_load_16_s", -1))
        assertEquals(65535, instance.invoke("i32_load_16_u", -1))

        assertEquals(100, instance.invoke("i32_load_8_s", 100))
        assertEquals(200, instance.invoke("i32_load_8_u", 200))
        assertEquals(20000, instance.invoke("i32_load_16_s", 20000))
        assertEquals(40000, instance.invoke("i32_load_16_u", 40000))
    }
}
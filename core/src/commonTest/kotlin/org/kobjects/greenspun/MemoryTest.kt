package org.kobjects.greenspun

import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.Bool
import org.kobjects.greenspun.core.type.F64
import org.kobjects.greenspun.core.type.I32
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

            Export("data", Func(Bool) {
                Return((mem.load8U(0).i32 Eq 65)
                        And (mem.load8U(3).i32 Eq 0x0c2)
                        And (mem.load8U(4).i32 Eq 0x0a7)
                        And (mem.load8U(6).i32 Eq 0)
                        And (mem.load8U(19).i32 Eq 0)
                        And (mem.load8U(20).i32 Eq 87)
                        And (mem.load8U(23).i32 Eq 77)
                        And (mem.load8U(24).i32 Eq 0)
                        And (mem.load8U(1023).i32 Eq 0))
            })

            Export("i32_load_8_s", Func(I32) {
                val i = Param(I32)
                mem.store8(8, i)
                Return(mem.load8S(8).i32)
            })

        }

        val instance = module.instantiate()

        assertEquals(1, instance.invoke("data"))

        assertEquals(-1, instance.invoke("i32_load_8_s", -1))

        assertEquals(100, instance.invoke("i32_load_8_s", 100))

    }
}
package org.kobjects.greenspun

import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.Bool
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

            val data = Func(Bool) {
                Return((mem.load8U(I32, 0) Eq 65)
                        And (mem.load8U(I32, 3) Eq 0x0c2)
                        And (mem.load8U(I32, 4) Eq 0x0a7)
                        And (mem.load8U(I32, 6) Eq 0)
                        And (mem.load8U(I32, 19) Eq 0)
                        And (mem.load8U(I32, 20) Eq 87)
                        And (mem.load8U(I32, 23) Eq 77)
                        And (mem.load8U(I32, 24) Eq 0)
                        And (mem.load8U(I32, 1023) Eq 0))
            }

            Export("data", data)
        }

        val instance = module.instantiate()

        assertEquals(1, instance.invoke("data"))
    }
}
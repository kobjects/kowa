package org.kobjects.greenspun

import org.kobjects.greenspun.core.instance.instantiate
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.memory.MemorySize
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
                Data("")
                ExportFunc("memsize", I32) { +MemorySize() }
            }.instantiate().invoke("memsize")
        )

        assertEquals(
            1,
            Module {
                Data("x")
                ExportFunc("memsize", I32) { +MemorySize() }
            }.instantiate().invoke("memsize")
        )
    }
}
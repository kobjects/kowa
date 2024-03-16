package org.kobjects.greenspun

import org.kobjects.greenspun.core.binary.hexDump
import org.kobjects.greenspun.core.control.If
import org.kobjects.greenspun.core.control.While
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32
import org.kobjects.greenspun.core.type.I64
import org.kobjects.greenspun.core.type.Str
import org.kobjects.greenspun.core.type.Void
import kotlin.test.Test
import kotlin.test.assertEquals

class FactorialTest {


    val module = Module {

        ExportFunc("factorial", I64) {
            val value = Param(I64)

            val result = Local(1L)

            +While(value Gt 1L,
                Block {
                    +Set(result, result * value)
                    +Set(value, value - 1L)
                }
            )
            +result
        }
    }

    @Test
    fun codeTest() {
        assertEquals(
                "00 61 73 6D 01 00 00 00 01 06 01 60 01 7E 01 7E\n" +
                "03 02 01 00 07 0D 01 09 66 61 63 74 6F 72 69 61\n" +
                "6C 00 00 0A 28 01 26 01 01 7E 42 01 21 01 02 40\n" +
                "03 40 20 00 42 01 55 45 0D 01 20 01 20 00 7E 21\n" +
                "01 20 00 42 01 7D 21 00 0B 0B 20 01 0B ", module.toWasm().hexDump())
    }
}
package org.kobjects.greenspun

import org.kobjects.greenspun.core.binary.hexDump
import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I64
import kotlin.test.Test
import kotlin.test.assertEquals

class FactorialTest {


    val module = Module {

        ExportFunc("factorial", I64) {
            val value = Param(I64)

            val result = Var(1L)

            +While(value Gt 1L) {
                +Set(result, result * value)
                +Set(value, value - 1L)
            }
            +result
        }
    }

    val instance = module.instantiate()

    @Test
    fun calculationTest() {
        assertEquals(1L, instance.invoke("factorial", 1L))
        assertEquals(2L, instance.invoke("factorial", 2L))
        assertEquals(6L, instance.invoke("factorial", 3L))
        assertEquals(24L, instance.invoke("factorial", 4L))
    }

    @Test
    fun codeTest() {
        assertEquals(
                "00 61 73 6D 01 00 00 00 01 06 01 60 01 7E 01 7E\n" +
                        "03 02 01 00 07 0D 01 09 66 61 63 74 6F 72 69 61\n" +
                        "6C 00 00 0A 1E 01 1C 01 01 7E 42 01 21 01 03 0D\n" +
                        "00 20 01 20 00 7E 21 01 20 00 42 01 7D 21 00 0B\n" +
                        "20 01 0B ", module.toWasm().hexDump())
    }
}
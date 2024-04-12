package org.kobjects.greenspun

import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.FuncRef
import org.kobjects.greenspun.core.type.I32
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementTest {

    @Test
    fun indirectCallTest() {
        val module = Module {
            val table = Table(FuncRef, 10)

            val constI32A = Func(I32) { Return(65) }
            val constI32B = Func(I32) { Return(66) }

            table.elem(7, constI32A)
            table.elem(9, constI32B)

            Export("call-7", Func(I32) {
                Return(table[7, I32]())
            })
            Export("call-9", Func(I32) {
                Return(table[9, I32]())
            })
        }

        val instance = module.instantiate()

        assertEquals(65, instance.invoke("call-7"))
        assertEquals(66, instance.invoke("call-9"))
    }

}
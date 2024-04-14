package org.kobjects.greenspun

import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelTest {

    val module = Module {
        Export("block", Func(I32) {
            val exit = Label()
            Block(I32) {
                Push(1)
                Br(exit)
                Push(0)
            }
        })

        Export("loop1", Func(I32) {
            val i = Var(0)
            val exit = Label()
            Block(I32) {
                val cont = Label()
                Loop(I32) {
                    i.set(i + 1)
                    If(i Eq 5) {
                        Br(exit, 5)
                    }
                    Br(cont)
                }
            }
        })

    }

    val instance = module.instantiate()

    @Test
    fun test() {
        assertEquals(1, instance.invoke("block"))
    }

}
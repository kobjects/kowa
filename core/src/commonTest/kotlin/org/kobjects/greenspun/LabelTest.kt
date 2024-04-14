package org.kobjects.greenspun

import org.kobjects.greenspun.core.module.Module
import org.kobjects.greenspun.core.type.I32
import kotlin.test.Test
import kotlin.test.assertEquals

class LabelTest {

    val module = Module {
        Export("block", Func(I32) {
            val exit = Label()
            +Block(I32) {
                Br(exit, 1)
                Push(0)
            }
        })

        Export("loop1", Func(I32) {
            val i = Var(0)
            val exit = Label()
            +Block(I32) {
                val cont = Label()
                +Loop(I32) {
                    i.set(i + 1)
                    If(i Eq 5) {
                        Br(exit, 5)
                    }
                    Br(cont)
                }
            }
        })

        Export("loop2", Func(I32) {
            val i = Var(0)
            val exit = Label()
            +Block(I32) {
                val cont = Label()
                +Loop(I32) {
                    i.set(i + 1)
                    If (i Eq 5) {
                        Br(cont)
                    }
                    If (i Eq 8) {
                        Br(exit, i)
                    }
                    i.set(i + 1)
                    Br(cont)
                }
            }
        })

        Export("loop3", Func(I32) {
            val i = Var(0)
            val exit = Label()
            +Block(I32) {
                val cont = Label()
                +Loop(I32) {
                    i.set(i + 1)
                    If(i Eq 5) {
                        Br(exit, i)
                    }
                    Push(i)
                }
            }
        })

        Export("loop4", Func(I32) {
          val max = Param(I32)
          val i = Var(1)
          val exit = Label()
          +Block(I32) {
              val cont = Label()
              +Loop(I32) {
                  i.set(i + i)
                  If(i GtU max) {
                      Br(exit, i)
                  }
                  Br(cont)
              }
          }
        })

        Export("loop5", Func(I32) {
            +(Loop(I32) { Push(1) } + 1)
        })

        Export("loop6", Func(I32) {
            val cont = Label()
            +Loop(I32) {
               BrIf(cont, false)
               Push(3)
           }
        })
    }

    val instance = module.instantiate()

    @Test
    fun test() {
        assertEquals(1, instance.invoke("block"))
        assertEquals(5, instance.invoke("loop1"))
        assertEquals(8, instance.invoke("loop2"))
        assertEquals(1, instance.invoke("loop3"))
        assertEquals(16, instance.invoke("loop4", 8))
        assertEquals(2, instance.invoke("loop5"))
        assertEquals(3, instance.invoke("loop6"))
    }

}
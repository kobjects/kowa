package org.kobjects.kowa

import org.kobjects.kowa.core.module.Module
import org.kobjects.kowa.core.type.I32
import kotlin.test.Test
import kotlin.test.assertEquals

class I32Test {

    val module = Module {
        Export("add", Func(I32) {
            val x = Param(I32)
            val y = Param(I32)
            Return(x + y)
        })
    }

    val instance = module.instantiate()

    fun invoke(name: String, vararg param: Any): Any = instance.invoke(name, *param)

    @Test
    fun testAdd() {
        assertEquals(2, invoke("add",1, 1))
        assertEquals(1, invoke("add", 1, 0))
        assertEquals(-2, invoke("add", -1, -1))
        assertEquals(0, invoke("add", -1, 1))
        assertEquals(0x80000000.toInt(), invoke("add", 0x7fffffff, 1))
        assertEquals(0x7fffffff, invoke("add", 0x80000000.toInt(), -1))
        assertEquals(0, invoke("add", 0x80000000.toInt(), 0x80000000.toInt()))
        assertEquals(0x40000000, invoke("add", 0x3fffffff, 1))
    }

}
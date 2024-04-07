package org.kobjects.greenspun.core.runtime

class Stack {

    val stack = mutableListOf<Any>()

    val size: Int
        get() = stack.size


    fun peekAny(offset: Int = 0) = stack[size - offset - 1]

    fun peekF32(offset: Int = 0) = stack[size - offset - 1] as Float

    fun peekF64(offset: Int = 0) = stack[size - offset - 1] as Double

    fun peekI32(offset: Int = 0) = stack[size - offset - 1] as Int
    fun peekI64(offset: Int = 0) = stack[size - offset - 1] as Long

    fun peekU32(offset: Int = 0) = peekI32(offset).toUInt()
    fun peekU64(offset: Int = 0) = peekI64(offset).toULong()


    fun replaceF32(count: Int, value: Float) {
        for (i in 0 until count) {
            stack.removeLast()
        }
        stack.add(value)
    }

    fun replaceF64(count: Int, value: Double) {
        for (i in 0 until count) {
            stack.removeLast()
        }
        stack.add(value)
    }

    fun replaceI32(count: Int, value: Int) {
        for (i in 0 until count) {
            stack.removeLast()
        }
        stack.add(value)
    }

    fun replaceI64(count: Int, value: Long) {
        for (i in 0 until count) {
            stack.removeLast()
        }
        stack.add(value)
    }

    fun replaceU32(count: Int, value: UInt) = replaceI32(count, value.toInt())

    fun replaceU64(count: Int, value: UInt) = replaceI64(count, value.toLong())

    fun replaceAny(count: Int, value: Any) {
        for (i in 0 until count) {
            stack.removeLast()
        }
        stack.add(value)
    }

    fun replaceBool(count: Int, value: Boolean) {
        for (i in 0 until count) {
            stack.removeLast()
        }
        pushBool(value)
    }

    fun popAny() = stack.removeLast()

    fun popF32() = stack.removeLast() as Float

    fun popF64() = stack.removeLast() as Double

    fun popI32() = stack.removeLast() as Int


    fun popI64() = stack.removeLast() as Long

    fun popU32() = (stack.removeLast() as Int).toUInt()

    fun popU64() = (stack.removeLast() as Long).toULong()

    fun popBool() = popI32() != 0

    fun pushAny(value: Any) = stack.add(value)

    fun pushI32(value: Int) = stack.add(value)

    fun pushI64(value: Long) = stack.add(value)

    fun pushF32(value: Float) = stack.add(value)

    fun pushF64(value: Double) = stack.add(value)

    fun pushBool(value: Boolean) =
        stack.add(if (value) 1 else 0)

    fun pushU32(value: UInt) =
        stack.add(value.toInt())

    fun pushU64(value: ULong) =
        stack.add(value.toLong())


}
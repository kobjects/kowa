package org.kobjects.greenspun.core.binary

class Wasm(
    val code: ByteArray,
    val endPositions: Map<Int, Int>,
    val elsePositions: Map<Int, Int>
) {
}
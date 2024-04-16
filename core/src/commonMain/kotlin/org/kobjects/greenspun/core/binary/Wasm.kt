package org.kobjects.greenspun.core.binary

class Wasm(
    val code: ByteArray,

    /** Maps block start positions to the corresponding end positions */
    val endPositions: Map<Int, Int>,

    /** Maps block start positions to the corresponding else positions */
    val elsePositions: Map<Int, Int>
) {
}
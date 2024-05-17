package org.kobjects.kowa.binary

/**
 * A block of Wasm code, including maps from block start positions to the corresponding
 * else and end positions.
 */
class Wasm(
    val code: ByteArray,

    /** Maps block start positions to the corresponding end positions */
    val endPositions: Map<Int, Int>,

    /** Maps block start positions to the corresponding else positions */
    val elsePositions: Map<Int, Int>
) {
}
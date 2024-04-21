package org.kobjects.greenspun.core.memory

import org.kobjects.greenspun.binary.Wasm

class DataImpl(
    val offset: Wasm?,
    val data: ByteArray
)
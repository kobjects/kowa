package org.kobjects.kowa.core.memory

import org.kobjects.kowa.binary.Wasm

class DataImpl(
    val offset: Wasm?,
    val data: ByteArray
)
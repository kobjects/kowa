package org.kobjects.greenspun.core.instance

import org.kobjects.greenspun.core.func.FuncImport
import org.kobjects.greenspun.core.module.Module


fun Module.instantiate(importObject: ImportObject = ImportObject()): Instance {

    return Instance(this, importObject)
}
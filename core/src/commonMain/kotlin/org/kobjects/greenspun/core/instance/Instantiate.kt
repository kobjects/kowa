package org.kobjects.greenspun.core.instance

import org.kobjects.greenspun.core.func.FuncImport
import org.kobjects.greenspun.core.module.Module


fun Module.instantiate(importObject: ImportObject = ImportObject()): Instance {
    val funcImports = funcs.filterIsInstance<FuncImport>()

    val resolvedImports = List<((Instance, Array<Any>) -> Any)>(funcImports.size) {
        val funcImport = funcImports[it]
        importObject.funcs[funcImport.module to funcImport.name] ?: throw IllegalStateException(
            "Import function ${funcImport.module}.${funcImport.name} not found.")
    }
    return Instance(this, resolvedImports)
}
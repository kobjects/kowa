package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.type.FuncType
import org.kobjects.greenspun.runtime.Stack

class ForwardDeclaration(override val index: Int, override val type: FuncType) : FuncInterface {
    

    override fun call(context: Stack) {
      val resolved = context.instance.module.funcs[index]
      require(resolved !is ForwardDeclaration) {
          throw UnsupportedOperationException("Unresolved forward declaration")
      }
      return resolved.call(context)
    }
}
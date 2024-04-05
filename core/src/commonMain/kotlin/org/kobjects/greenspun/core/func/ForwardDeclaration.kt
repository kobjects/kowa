package org.kobjects.greenspun.core.func

import org.kobjects.greenspun.core.tree.Node
import org.kobjects.greenspun.core.type.FuncType

class ForwardDeclaration(override val index: Int, override val type: FuncType) : FuncInterface {
    

    override fun call(context: LocalRuntimeContext, vararg params: Any): Any {
      val resolved = context.instance.module.funcs[index]
      require(resolved !is ForwardDeclaration) {
          throw UnsupportedOperationException("Unresolved forward declaration")
      }
      return resolved.call(context, *params)
    }
}
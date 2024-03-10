package org.kobjects.greenspun.core.tree

enum class BinaryOperator(
    val typeSupport: TypeSupport,
    val symbol: String? = null
) {
    PLUS(TypeSupport.ALL,"+"),
    DIV(TypeSupport.ALL,"/"),
    TIMES(TypeSupport.ALL,"*"),
    MINUS(TypeSupport.ALL,"-"),

    REM(TypeSupport.INT_ONLY,"%"),

    COPYSIGN(TypeSupport.FLOAT_ONLY),
    MIN(TypeSupport.FLOAT_ONLY),
    MAX(TypeSupport.FLOAT_ONLY),

    AND(TypeSupport.INT_ONLY),
    OR(TypeSupport.INT_ONLY),
    XOR(TypeSupport.INT_ONLY),

    SHL(TypeSupport.INT_ONLY),
    SHR(TypeSupport.INT_ONLY),

    ROTL(TypeSupport.INT_ONLY),
    ROTR(TypeSupport.INT_ONLY);

    override fun toString() = symbol ?: name.titleCase()
}
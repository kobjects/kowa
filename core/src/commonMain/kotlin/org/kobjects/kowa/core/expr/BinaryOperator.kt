package org.kobjects.kowa.core.expr

enum class BinaryOperator(
    val typeSupport: TypeSupport,
    val symbol: String? = null
) {
    ADD(TypeSupport.ALL,"+"),
    DIV_S(TypeSupport.ALL,"/"),
    DIV_U(TypeSupport.INT_ONLY),
    MUL(TypeSupport.ALL,"*"),
    SUB(TypeSupport.ALL,"-"),

    REM_S(TypeSupport.INT_ONLY,"%"),
    REM_U(TypeSupport.INT_ONLY),

    COPYSIGN(TypeSupport.FLOAT_ONLY),
    MIN(TypeSupport.FLOAT_ONLY),
    MAX(TypeSupport.FLOAT_ONLY),

    AND(TypeSupport.INT_ONLY),
    OR(TypeSupport.INT_ONLY),
    XOR(TypeSupport.INT_ONLY),

    ROTL(TypeSupport.INT_ONLY),
    ROTR(TypeSupport.INT_ONLY),

    SHL(TypeSupport.INT_ONLY),
    SHR_S(TypeSupport.INT_ONLY),
    SHR_U(TypeSupport.INT_ONLY);


    override fun toString() = symbol ?: name.titleCase()
}
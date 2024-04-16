package org.kobjects.greenspun.core.expr

import org.kobjects.greenspun.core.type.*

enum class UnaryOperator(
    val deviantResultType: Type? = null,
    vararg val supportedTypes: Type
) {
    ABS(null, F32, F64),

    CEIL(null, F32, F64),
    CLZ(null, I32, I64),
    CTZ(null, I32, I64),

    FLOOR(null, F32, F64),
    POPCNT(null, I32, I64),

    NEG,
    NEAREST(null, I32, I64),
    NOT(null, I32, I64, Bool),

    SQRT(null, F32, F64),
    TRUNC,

    EXTEND_S(I64, I32),
    EXTEND_U(I64, I32),

    TRUNC_TO_I32_S(I32, F32, F64),
    TRUNC_TO_I32_U(I32, F32, F64),
    TRUNC_TO_I64_U(I32, F32, F64),
    TRUNC_TO_I64_S(I32, F32, F64),

    WRAP(I32, I64),

    PROMOTE(F64, F32),
    DEMOTE(F32, F64),

    CONVERT_TO_F32_S(F32, I32, I64),
    CONVERT_TO_F32_U(F32, I32, I64),
    CONVERT_TO_F64_S(F64, I32, I64),
    CONVERT_TO_F64_U(F64, I32, I64),

    REINTERPRET();



    override fun toString() = name.titleCase()
}
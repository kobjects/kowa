# Instruction Name Mapping

## Built-in infix functions

| Kt DSL | Wasm I32  | Wasm I64  | Wasm F32 | Wasm F64 |
|--------|-----------|-----------|----------|----------|
| +      | I32.add   | I64.add   | F32.mul  | F64.mul  |
| -      | I32.sub   | I64.sub   | F32.mul  | F64.mul  |
| *      | I32.mul   | I64.mul   | F32.mul  | F64.mul  |
| /      | I32.div_s | I64.div_s | F32.div  | F64.div  |
| %      | I32.rem_s | I64.rem_s | F32.rem  | F64.rem  |
| And    | I32.and   | I64.and   |          |          |
| DivU   | I32.div_u | I64.div_u |          |          |
| Or     | I32.or    | I64.or    |          |          |
| RemU   | I32.rem_u | I64.rem_u |          |          |
| Rotl   | I32.rotl  | I64.rotl  |          |          |   
| Shl    | I32.shl   | I64.shl   |          |          |
| Shr    | I32.shr_s | I64.shr_s |          |          |
| ShrU   | I32.shr_u | I64.shr_u |          |          |
| Xor    | I32.xor   | I64.xor   |          |          |

## Built-in functions with two arguments

| Kt DSL   | F32          | F64          |
|----------|--------------|--------------|
| CopySign | F32.copysign | F64.copysign |
| Max      | F32.max      | F64.max      |
| Min      | F32.min      | F64.min      |


## Built-in single argument operator and functions

| Kt DSL  | Wasm I32   | Wasm I64   | Wasm F32  | Wasm F64  |
|---------|------------|------------|-----------|-----------|
| Unary - | *          | *          | F32.neg   | F64.neg   | 
| Abs     |            |            | F32.abs   | F64.abs   |
| Ceil    |            |            | F32.ceil  | F64.ceil  |
| Clz     | I32.clz    | I64.clz    |           |           | 
| Ctz     | I32.ctz    | I64.ctz    |           |           |
| Floor   |            |            | F32.floor | F64.floor |
| Popcnt  | I32.popcnt | I64.popcnt |           |           |      

*) Mapped to multiple Wasm instructions for convenience.

## Relational Operations

| Kt DSL | Wasm I32 | Wasm I64 | Wasm F32 | Wasm F64 |
|--------|----------|----------|----------|----------|
| Eq     | I32.eq   | I64.eq   | F32.eq   | F64.eq   |
| Ge     | I32.ge_s | I64.ge_s | F32.ge   | F64.ge   |
| GeU    | I32.ge_u | I64.ge_u |          |          |
| Gt     | I32.gt_s | I64.gt_s | F32.gt   | F64.gt   |
| GtU    | I32.gt_u | I64.gt_u |          |          |
| Le     | I32.le_s | I64.le_s | F32.le   | F64.le   |
| LeU    | I32.le_u | I64.le_u |          |          |
| Lt     | I32.lt_s | I64.lt_s | F32.lt   | F64.lt   |
| LtU    | I32.lt_u | I64.lt_u |          |          |
| Ne     | I32.ne   | I64.ne   | F32.ne   | F64.ne   |


## Type Conversions

| To \ From | I32               | I64               | F32                 | F64                 |
|-----------|-------------------|-------------------|---------------------|---------------------|
| I32       |                   | I32.wrap_i64      | I32.Reinterpret_f32 |                     |
| - DSL     |                   | Wrap              | Reinterpret         |                     |
| I32 (S)   |                   |                   | I32.trunc_f32_s     | I32.trunc_f64_s     |
| - DSL     |                   |                   | TruncToI32          | TruncToI32          |
| I32 (U)   |                   |                   | I32.trunc_f32_u     | I32.trunc_f64_u     |
| - DSL     |                   |                   | TruncToI32U         | TruncToI32U         |
| I64       |                   |                   |                     | I64.Reinterpret_f64 | 
| - DSL     |                   |                   |                     | Reinterpret         |
| I64 (S)   | I64.extend_i32_s  |                   | I64.trunc_f32_s     | I64.trunc_f64_s     |
| - DSL     |                   |                   | TruncToI64          | TruncToI64          |
| I64 (U)   | I64.extend_i32_u  |                   | I64.trunc_f32_u     | I64.trunc_f64_u     |
| - DSL     |                   |                   | TruncToI64U         | TruncToI64U         |
| F32       | F32.convert_i32_s | F32.convert_i64_s |                     | F32.demote_f64      |
| -DSL      | ConvertToF32      | ConvertToF32      |                     | Demote              |
|           | F32.convert_i32_u | F32.convert_i64_u |                     |                     |
|           | ConvertToF32U     | ConvertToF32U     |                     |                     |
| F64       | F64.convert_i32_s | F64.convert_i64_s | F63.promote_f32     |                     |
| -DSL      | ConvertToF64      | ConvertToF64      | Promote             |                     |
|           | F64.convert_i32_u | F64.convert_i64_u |                     |                     |
|           | ConvertToF64U     | ConvertToF64U     |                     |                     |


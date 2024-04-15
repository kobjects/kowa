# A Kotlin DSL for WebAssembly

## But Why?

The initial motivation for this project was just to see how far one can take Kotlin DSLs.

That said, it might actually be useful for situations where one needs to generate Wasm code
in a general kotlin context.

## Overview

### General

To avoid confusion with "regular" Kotlin, all top level constructs use camel case 
starting with an uppercase letter.

### Modules

Modules are declared using the "Module" function. The DSL parameter contains the declarations
of all the sections. Example:

```kt
val module = Module {
}
```

### Functions

Functions are declared using the "Func" function inside a module, taking the return value type as a direct 
argument and the function body as a DSL parameter. Example: 

```kt
val module = Module {
  val answer = Func(I32) {
    Return (42)
  }
}
```

#### Function Parameters

Function parameters are declared inside the "body" using the "Param" function. Example:

```kt
val sqr = Func(I32) {
  val x = Param(I32) 
  Return (x * x) 
}
```

#### Function Exports

Functions (and other constructs) can be exported using `Export()`. A full example for 
a WebAssembly module exporting a function "sqr" for calculating the square of 32 bit integers is:

```kt
val module = Module {
  Export("sqr", Func(I32) {
    val x = Param(I32)
    Return (x * x)
  })
}
```

#### Invocation from Kotlin

The exported `sqr()`-function can be invoked from Kotlin by instantiating the module and
then invoking the exported function: 

```kt
val instance = module.instantiate()
val sqr4 = instance.invoke("sqr", 4)
println("The square of 4 is: $sqr4")
```

### Instructions

For mapping Wasm instructions to our DSL, we divide them into two groups:

- We'll call instructions that push a value on the stack "expressions".
- Instructions that don't push a value on the stack we'll call "statements".

Expressions can be nested and statements can take expressions as parameters, but
a line of code always has to be a statement. 

Expressions can be turned into statements by either dropping their return value
using the `Drop()` statement -- or by putting their return value on the
stack using `Push()`. `Push()` isn't really a Wasm instructions, it just makes pushing the result of
an expression on the stack explicit for our Kotlin DSL.  Unary plus can be used as a shorthand for `Push()`.

#### Mathematical and bitwise operations

Mathematical operations that match an overloadable Kotlin operator are mapped accordingly.
If there are signed and unsigned variants of the operation, the signed variant is mapped to the
operator and the unsigned operation is mapped to a Kotlin infix function.

We have already seen `I32.mul` mapped to the multiplication operator in the square example above.

Other binary operations such as `shl`, `xor` and `or` are mapped to kotlin infix operations,
starting with an uppercase letter.

##### Built-in infix functions

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

##### Built-in functions with two arguments

| Kt DSL   | F32          | F64          |
|----------|--------------|--------------|
| CopySign | F32.copysign | F64.copysign |
| Max      | F32.max      | F64.max      |
| Min      | F32.min      | F64.min      |


##### Built-in single argument operator and functions

| Kt DSL  | Wasm I32   | Wasm I64   | Wasm F32  | Wasm F64  |
|---------|------------|------------|-----------|-----------|
| Unary - | *          | *          | F32.neg   | F64.neg   | 
| Abs     |            |            | F32.abs   | F64.abs   |
| Ceil    |            |            | F32.ceil  | F64.ceil  |
| Clz     | I32.clz    | I64.clz    |           |           | 
| Ctz     | I32.ctz    | I64.ctz    |           |           |
| Floor   |            |            | F32.floor | F64.floor |
| Popcnt  | I32.popcnt | I64.popcnt |           |           |      


#### Relational Operations

#### Conversion Operations


## More Examples 

More examples can be found in the [test directory](https://github.com/kobjects/greenspun/tree/main/core/src/commonTest/kotlin/org/kobjects/greenspun) of the project.

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

```
val module = Module {
}
```

### Functions

Functions are declared using the "Func" function inside a module, taking the return value type as a direct 
argument and the function body as a DSL parameter. Example: 

```
val module = Module {
  val answer = Func(I32) {
    Return (42)
  }
}
```

Function parameters are declared inside the "body" using the "Param" function. Example:

```
val sqr = Func(I32) {
  val x = Param(I32) 
  Return (x * x) 
}
```

### Function Exports

Functions (and other constructs) can be exported using `Export()`. A full example for 
a WebAssembly module exporting a function "sqr" for calculating the square of 32 bit integers is:

```
val module = Module {
  Export("sqr", Func(I32) {
    val x = Param(I32)
    Return (x * x)
  })
}
```

### Invocation from Kotlin

The exported `sqr()`-function can be invoked from Kotlin by instantiating the module and
then invoking the exported function: 

```
val instance = module.instantiate()
val sqr4 = instance.invoke("sqr", 4)
println("The square of 4 is: $sqr4")
```


## More Examples 

More examples can be found in the [test directory](https://github.com/kobjects/greenspun/tree/main/core/src/commonTest/kotlin/org/kobjects/greenspun) of the project.
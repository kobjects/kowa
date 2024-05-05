Back to [README.md](../README.md)

# DSL Description

## Modules

Modules are declared using the "Module" function. The DSL parameter contains the declarations
of all the sections. Example:

```kt
val module = Module {
}
```

## Functions

Functions are declared using the "Func" function inside a module, taking the return value type as a direct 
argument and the function body as a DSL parameter. Example: 

```kt
val module = Module {
  val answer = Func(I32) {
    Return (42)
  }
}
```

### Function Parameters

Function parameters are declared inside the "body" using the "Param" function. Example:

```kt
val sqr = Func(I32) {
  val x = Param(I32) 
  Return (x * x) 
}
```

### Function Exports

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

### Invocation from Kotlin

The exported `sqr()`-function can be invoked from Kotlin by instantiating the module and
then invoking the exported function: 

```kt
val instance = module.instantiate()
val sqr4 = instance.invoke("sqr", 4)
println("The square of 4 is: $sqr4")
```

### Local Variables 

Local variables are declared similar to parameters using `Var()` or `Const()`.
Instead of the type argument, they take an expression defining the initial value. 

Immutable local variables are not supported by Wasm directly -- the mutability
property is checked by the DSL only. 

Mutable local variables can be assigned new values by calling `.set()`.

### Function imports

Functions can be imported using the `ImportFunc()` function.

```kt
  val LogStr = ImportFunc("console", "logStr") { Param(I32, I32) } 
```

## Instructions, Expressions and Statements

To avoid confusion with "regular" Kotlin, all top level constructs use camel case starting with an uppercase
letter, e.g. Wasm `if` becomes `If` in the DSL.

For mapping Wasm instructions to our DSL, we divide them into two groups:

- We'll call instructions that push a value on the stack "expressions".
- Instructions that don't push a value on the stack we'll call "statements".

Expressions can be nested and statements can take expressions as parameters, but
a line of code always has to be a statement. 

Expressions can be turned into statements by either dropping their return value
using the `Drop()` statement -- or by putting their return value on the
stack using `Push()`. Push isn't really a Wasm instructions, it just makes pushing the result of
an expression on the stack explicit for our Kotlin DSL. The unary plus operator can be used as a 
shorthand for `Push()`.

### Literals and number types

Literal values -- or kotlin numbers in general -- are mapped as follows to Wasm types:

| Kotlin type  | Wasm type |
|--------------|-----------|
| Boolean, Int | I32       |
| Long         | I64       |
| Float        | F32       |
| Double       | F64       |

Typically, Kotlin numbers can be used directly and will be converted to expressions implicitly.
The main exception is the first parameter of an operator: in this case, they need to be wrapped 
in a `Const()` call. 


### Mathematical and bitwise operations

Mathematical operations that match an overloadable Kotlin operator are mapped accordingly.
If there are signed and unsigned variants of the operation, the signed variant is mapped to the
operator and the unsigned operation is mapped to a Kotlin infix function.

We have already seen `I32.mul` mapped to the multiplication operator in the square example above.

Other binary operations such as `shl`, `xor` and `or` are mapped to kotlin infix operations,
starting with an uppercase letter.



### Relational Operations and `Bool`

Unfortunately, Kotlin operator overloading for relational operations
doesn't allow us to change the return type, so we map them to infix
functions named after the corresponding Wasm instructions.

To simplify combining comparisons, we provide a special type named `Bool`
that maps to I32 but only can hold the values 0 (false) and 1 (true) .


### Blocks and Control instructions

Blocks are mapped to Kotlin functions with "builder" parameters. Branch labels
are created using the `Label()` constructor immediately preceding the target block.

For instance, a simple loop counting a variable `i` down looks as follows:

```kt
val i = Var(5)
val cont = Label()
Loop {
  i.set(i - 1)
  BrIf (cont, i Gt 1)
}
```

#### Conditions


For conditional statements, an optional else block can be provided via the `.Else()` method:

```kt
If (condition) {
  // Do something
}.Else {
  // Do something else
}
```

#### Blocks returning a value

Blocks returning a value take the return type as a parameter and are treated as expressions:

```kt
Return (Block(I32) {
  Push(42)
})
```

For "If", there is an "expression" variant that doesn't have an explicit return type but works much like ternaries in other languages:

```kt
Return (If(condition, 42, 43))
```

#### Convenience Control instructions: `While` and `For`

In addition to the Wasm `Loop` block, our DSL provides convenience `While()` and `For()` functions.

`While()` will iterate the following block of instructions until the condition expression is false.

`For()` takes two to three arguments:

- The initial loop variable value
- The maximum loop variable value. The iteration will continue until this value is reached, exluding the maximum value.
- An optional step value

Both constructs will map to a Wasm `loop` inside a `block` with some additional instructions for
the condition and correspdonding branch.  


## Forward Declarations


The mappings described so far allow us to port a slightly more complex example than `sqr()`from the 
Wasm test suite:

```kt
val factorialIterative = Func(I64) {
  val n = Param(I64)
  val res = Var(n)
  For (2L, n) {
    res.set(res * it)
  }
  Return(res)
}
```


Unfortuantely, assigning function declarations to kotlin variables means that the variable is not
available inside the function declaration. We need to use forward declarations to work around this
limitation:

```kt
val factorialRecursive = ForwardDecl(I64) { Param(I64) }

Implementation(factorialRecursive) {
  val n = Param(I64)
  Return(If(n Eq 0L, 1L, n * factorialRecursive(n - 1L)))
}
```


## Memory and Data

Memory is declared using the `Memory` function, taking the
minimum and optional maximum size as parameters.

Although there is currently only one "memory", all memory access
is based on the reference returned from the memory declaration,
so it makes sense to keep hold of it in a variable.

```kt
val module = Module {
  val mem = Memory(1)
}
```

### Data

Memory can be statically initialized with data. If no 
offset is provided, the end of the previous data will
be used as the start offset. The reference returned from
the data declaration will refer to its offset. This
reference also has a "len" property which will provide
the byte size of the corrsponding data item.

```kt
val module = Module {
  val LogStr = ImportFunc("console", "logStr") { Param(I32, I32) } 

  val mem = Memory(1)

  val message = mem.data("FizzBuzz")

  val f = Func() {
    LogStr(message, message.len)
  }
}
```

### Memory Access Instructions

Wasm memory `load` and `store` instructions are mapped to array access
on a memory property indicating the access width, offset and align.

The following example implements two functions providing bytewise
memory read and write acces via `peek` and `poke` functions.

```kt
val module = Module {
  val mem = Memory(1)

  val peek = Func(I32) {
    val address = Param(I32)
    Return (mem.i32U8[address])
  }

  val poke = Func() {
    val address = Param(I32)
    val value = Param(I32)
    mem.i32U8[address] = value
  }
} 

```

It's possible to hold on to references with a given width,
align and offset:

```kt
val mem8 = mem.i32U8(offset = 1000)
```

It's also possible to provide an alignment and offset on access.
The access alignment overrides the general offset of the access type
property while the two offsets are added.


## Tables and Elements

Tables declarations are similar to memory declarations, but they take an additional type parameter --
which is required to be `FuncRef` for Wasm 1.

```kt
val table = Table(FuncRef, 10)
```

Similar to data declarations, the `elem` method on table references can be used to pre-fill table
elements:

```
val constI32A = Func(I32) { Return(65) }
table.elem(7, constI32A)
```

In order to call functions stored in tables, their type must be supplied explicitly.
For this purpose, we declare a function type returning an I32 value and not taking
any parameters:

```kt
val outI32 = Type(I32) {}
```

We can now call this function by invoking the table with the function index, the 
expected type and the function parameters (which we don't have in this simple case):

```kt
val call7 = Func(I32) {
  Return(table(7, outI32))
}
```

## Further Reading

Continue reading with [Instruction Name Mapping](instruction_name_mapping.md)
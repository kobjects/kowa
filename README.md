# A Kotlin DSL for WebAssembly

## Related documents

- [DSL Description](doc/dsl_description.md)
- [Instruction Name Mapping](doc/instruction_name_mapping.md)


## But Why?

The initial motivation for this project was just to see how far one can take Kotlin DSLs.

That said, it might be useful for situations where one needs to generate Wasm code in a general Kotlin context. 

In a way, this project constitutes a Wasm "macro assembler" with
the Kotlin as the "macro language" -- at the price of an "unusual" syntax.

## "Hello World"

To run our ["Hello World" example](https://github.com/kobjects/greenspun/blob/main/core/src/commonMain/kotlin/org/kobjects/greenspun/demo/HelloWorld.kt), start with checking out
the project from github using the following command:

```sh
git clone https://github.com/kobjects/greenspun.git
```

To check that everything works as expected, run the "HelloWorld" demo 
using the following command line:

```kt
./gradlew jvmRun -DmainClass=org.kobjects.greenspun.demo.HelloWorldKt
```

The output should look as follows:

```
Instantiating module:

Hello World


HelloWorld wasm binary code:

0061736d01000000010c0260047f7f7f  7f017f60000002230116776173695f73
6e617073686f745f7072657669657731  0866645f777269746500000302010105
030100010801010c01020a0f010d0041  014100410c410c10001a0b0b1b020041
000b0c48656c6c6f20576f726c640a00  410c0b0431323334
```

### Examining the Example

To play with the example, open the project in a Kotlin IDE, e.g. IntelliJ or Android Studio.

The "Hello World" example is located in 

```
core/src/commonMain/kotlin/org/kobjects/greenspun/demo
```


The core of this example looks as follows:

```kt
val module = Module {
  val memory = Export("memory", Memory(1))

  // Leave some space for the fd_write data vector and return value
  val helloWorld = memory.data(16, "Hello World\n")

  val fd_write = ImportFunc("wasi_snapshot_preview1", "fd_write", I32) { 
    Param(I32, I32, I32, I32) }

  val hello = Func {
    memory.i32[0] = helloWorld
    memory.i32[4] = helloWorld.len
    Drop(fd_write(1, 0, 1, 8))
  }

  Start(hello)
}
```

Here, we first declare memory and then use the data method to declare some
pre-filled memory locations.

Next, we import the `fd_write`-function form WASI in order to be able to
write to stdout. Unfortunately, the funtion signature is a bit 
complex and takes four I32 parameters: The file descriptor
(1 for stdout), the address of a vector of data to write, the length of the
vector and an address to store the numbers of bytes written. The
return value is an error code -- which is zero if there was no error.

Then we declare our own `hello` function which setS up the data
vector for fd_write and then calls it.

Finally, we declare that `hello` is the "start" function of the 
module -- which means that it's automatically run on instantiation.


### Running "Hello World" with the built-in Wasm Interpreter

The simplest way to run the example is to use the built-in interpreter:

```
val importObject = ImportObject()
importObject.addStdIoImpl()
module.instantiate(importObject)
```

To use our WASI stdout implementation, we first need to add it to the
import object. As our "Hello World" function is the "start" function
of our module, it will run automatically when we instnantiate the module.

To run the example from the command line, use the following command in
the project root directory:


### Getting the WASM binary code and running it elsewhere

The example used the following lines to print the binary
wasm code:

```
0061736d01000000010c0260047f7f7f  7f017f60000002230116776173695f73
6e617073686f745f7072657669657731  0866645f777269746500000302010105
03010001070a01066d656d6f72790200  0801010c01010a1d011b004100411036
00004104410c36000041014100410141  0810001a0b0b12010041100b0c48656c
6c6f20576f726c640a
```

This can be converted to a binary file using a tool like
[tomeko.net's online hex to file converter](
https://tomeko.net/online_tools/hex_to_file.php?lang=en).

The binary can then be run with `wasmtime`:

```
> wasmtime run hello.wasm
Hello World
```


When disassembling this code (e.g. using [wasm2wat](https://webassembly.github.io/wabt/demo/wasm2wat/)), we get the following output in regulare Wasm text format:

```wat
(module
  (type $t0 (func (param i32 i32 i32 i32) (result i32)))
  (type $t1 (func))
  (import "wasi_snapshot_preview1" "fd_write" (func $wasi_snapshot_preview1.fd_write (type $t0)))
  (func $f1 (type $t1)
    (i32.store align=1
      (i32.const 0)
      (i32.const 0))
    (i32.store align=1
      (i32.const 4)
      (i32.const 12))
    (drop
      (call $wasi_snapshot_preview1.fd_write
        (i32.const 1)
        (i32.const 0)
        (i32.const 1)
        (i32.const 12))))
  (memory $memory (export "memory") 1)
  (start $f1)
  (data $d0 (i32.const 16) "Hello World\0a"))

```


## More Examples 

Usage Examples can be found in the [test directory](https://github.com/kobjects/greenspun/tree/main/core/src/commonTest/kotlin/org/kobjects/greenspun) of the project.

## Next steps

- Complete multiple return type extension support (block parameters are currently missing).
- Look into orphan expression detection.

## Further Plans

- Port more Wasm tests
- Find a nice clean way to test the generated binary code
- Support more extensions 
- Perhaps support loading modules (would probably be useful to import trigonometric functions)
- Extend WASI coverage


## Known Issues

- There are no releases yet that can be directly referenced from Maven / Gradle. Given that there are
  very likely still some serious bugs in Wasm binary code
  generation, it's probably best to check out the
  full project anyway, simplifying fixes.

## Further Reading

Continue reading the with the [DSL description](doc/dsl_description.md).
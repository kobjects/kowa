# KoWA: A Kotlin WebAssembler

## But Why?

This project grew out of an attempt to look into "how far" one can take
Kotlin DSLs. 

It basically constitutes a Wasm "macro assembler" with
the Kotlin as the "macro language" -- at the price of an "unusual" syntax.

## Documentation

- Getting Started: ["Hello World"](doc/hello_world.md)
- [DSL Description](doc/dsl_description.md)
- [Instruction Name Mapping](doc/instruction_name_mapping.md)

## Examples

- [Hello World example source](https://github.com/kobjects/kowa/blob/main/core/src/commonMain/kotlin/org/kobjects/kowa/demo/HelloWorld.kt)
- [FizzBuzz example source](https://github.com/kobjects/kowa/blob/main/core/src/commonMain/kotlin/org/kobjects/kowa/demo/FizzBuzz.kt)
- [test directory](https://github.com/kobjects/greenspun/tree/main/core/src/commonTest/kotlin/org/kobjects/greenspun)

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


# groovy2kotlin

An utility converting [Groovy](http://groovy-lang.org/) sources to [Kotlin](https://kotlinlang.org/) language.

## requirements

The author uses:
- JDK 1.8
- Groovy 2.5.8

## todo first

- distinct classes and interfaces
- extend/implement
- Kotlin's `open` and `override`
- preserve javadoc
- move static members into the companion object
- `Some.class` -> `Some::class.java`
- replace use/import of anno `groovy.transform.CompileStatic` with Kotlin's analog
- translate `def` in formal params as`Any` probably, not `Object`
- bitwise
    - `~16` => `16.inv()`
- Standard functions unavailable in Kt:
    - `File.size()` => `File.length()`
    - `String.getBytes()` => `toByteArray()`
    - `String.replaceAll`

## todo requiring more analysis

- translate Groovy's implicit `return`
- "groovy truth" can't be translated straightly

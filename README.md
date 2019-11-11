# groovy2kotlin

An utility converting [Groovy](http://groovy-lang.org/) sources to [Kotlin](https://kotlinlang.org/) language.

The result of translation isn't 100% Kotlin-correct and it's required from the user to fix the rest manually,
but a significant amount of work can be saved for free.

Started 2019-11-08 by [zencd](https://github.com/zencd) to convert a project of mine
because I've not found any existing tools (and it's easy to write one).
The current implementation is very simple: it uses Groovy's internal
libs to parse source text into a well designed AST, then the tree is traversed and
translated to Kotlin by `GroovyToKotlin.groovy`. Gonna add type inference later
because it's required for certain transformations.

| features | features
|----------|------------- 
| ✔ Control structures | ✔ Most of expressions
| ✔ Classes | ✔ Bitwise expressions
| ✔ Closures | ✔ Static members grouped within companion
| ✔ Groovy's implicit imports |

## requirements

- JDK 1.8 (for running the tool)
- Groovy 2.5.8 (for running the tool)
- Kotlin 1.3 (produced code)

Others maybe supported but was not tested.

## using the tool

    git clone https://github.com/zencd/groovy2kotlin

## todo 1

- Distinct classes and interfaces
- Extend/implement
- Preserve javadoc (there is a way)
- Replace use/import of anno `groovy.transform.CompileStatic` with Kotlin's analog
- Probably translate `def` → `Any` in formal params, not `Object`
- Kotlin disallows use of `Map`/`List` without generics specified explicitly
- Groovy allows implicit conversions like `String s = 1L`
- Groovy's regexps `/.+/`
- Arrays: `String[]` → `Array<String>`
- Mapping:
    - `Some.class` → `Some::class.java`
    - `~16` → `16.inv()`
    - `File.size()` → `.length()`
    - `String.getBytes()` → `.toByteArray()`
    - `String.replaceAll`
    - `String.length()` → `.length`
    - `List.each` → `.forEach`
- Groovy's shortcuts:
    - `File.text`, `String.execute`, etc

## todo 2, requires type inference

- Groovy's special use of bitwise operators on lists, streams, etc
- Translate Groovy's implicit `return`
- "Groovy truth" can't be translated straight
- Kotlin's `open` and `override` (Kotlin is strict about their presence/absence)

# groovy2kotlin

An utility converting [Groovy](http://groovy-lang.org/) sources to [Kotlin](https://kotlinlang.org/) language.

The result of translation *isn't 100%* Kotlin-correct usually and user is required to review the result,
but a significant amount of work can be saved for free.

Started Nov 2019 by [zencd](https://github.com/zencd) in order to convert a project of mine
because I've not found any existing tools (and it's easy to write one by myself).
The current implementation is simple: it uses Groovy's internal
libs to parse source text into a well designed AST, then the tree is traversed and
translated to Kotlin. It's planned to add type inference to AST to allow more
sophisticated transformations.

Feel free to submit a bug, ticket, etc.

| Covered  | Covered
|----------|------------- 
| ✔ Control structures | ✔ Expressions
| ✔ Classes            | ✔ Bitwise expressions
| ✔ Closures           | ✔ Static members grouped within companion
| ✔ Groovy's implicit imports | ✔ Groovy's standard functions (WIP)

## Requirements

- JDK 1.8 (for running the tool)
- Groovy 2.5.8 (for running the tool)
- Kotlin 1.3 (produced code level)

Others may be supported but was not tested.

## Use

    git clone "https://github.com/zencd/groovy2kotlin"
    cd groovy2kotlin
    gradlew test jar
    groovy -cp build/libs/groovy2kotlin-0.0.1.jar -e="gtk.BulkProcessor.process('/groovy-project', '/output')"

# Structure

- `BulkProcessor.groovy` - converts a directory with Groovy files recursively
- `GroovyToKotlin.groovy` - the translator engine
- `test-data/input-output-tests/` - a set of input/output tests showing how the things gets translated

## Todo 1

- import static things like `import pack.Classe.Companion.staticMethod`
- Preserve javadoc (there is a way)
- Replace use/import of anno `groovy.transform.CompileStatic` with Kotlin's analog
- Probably translate `def` → `Any` in formal params, not `Object`
- Groovy allows implicit conversions like `String s = 1L`
- Groovy regexps without quotes: `/.+/`
- Groovy regex operator `==~`
- Groovy comparison operator `<=>`
- Mapping:
    - `Some.class` → `Some::class.java`
    - `~16` → `16.inv()`
    - `File.size()` → `.length()`
    - `String.getBytes()` → `.toByteArray()`
    - `String.length()` → `.length`
    - `List.eachWithIndex` → `.forEachIndexed` (params swapped!)
    - `a.is(b)` → `a === b`
- Groovy's shortcuts:
    - `File.text`, `String.execute`, etc

## Todo 2, requires type inference

- Groovy's special use of bitwise operators on lists, streams, etc
- Translate Groovy's implicit `return` (solved for certain cases)
- "Groovy truth" can't be translated straight
- Kotlin's `open` and `override` (a common, type-aware algorithm)

## Some history

- ✔ `String.replaceAll` → `"aaa".replace("a", "x")`
- ✔ local vars without an initializer
- ✔ `equals` → `override fun equals(other: Any?): Boolean`
- ✔ `hashCode` → `override fun hashCode(): Int`
- ✔ `toString` → `override fun toString(): String`
- ✔ `String[] res = [...]` → `res = arrayOf(...)`
- ✔ Range expression
- ✔ Extend/implement
- ✔ Generated fields `var url: String` must be initialized
- ✔ Distinct classes and interfaces
- ✔ Kotlin disallows use of `Map`/`List` without generics specified explicitly
- ✔ Groovy operator `in`
- ✔ anonymous classes 
- ✔ Inner classes: static and non-static
- ✔ `String[]` → `Array<String>`

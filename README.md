# groovy2kotlin

An utility converting [Groovy](http://groovy-lang.org/) sources to [Kotlin](https://kotlinlang.org/) language.

## requirements

- Groovy 2.5.8, at least tested against it

## todo first

- distinct classes and interfaces
- extend/implement
- Kotlin's `open` and `override`
- preserve javadoc
- move static members into the companion object
- `Some.class` -> `Some::class.java`
- replace use/import of anno `groovy.transform.CompileStatic` with Kotlin's analog
- translate `def` in formal params as`Any` probably, not `Object`

## todo requiring more analysis

- translate Groovy's implicit `return`
- "groovy truth" can't be translated straightly
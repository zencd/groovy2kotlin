# groovy2kotlin

An utility converting [Groovy](http://groovy-lang.org/) sources to [Kotlin](https://kotlinlang.org/) language.

## requirements

- Groovy 2.5.8, at least tested against it

## todo first

- auto-import classes like File, URL
- preserve javadoc
- move static members into the companion object
- `Some.class` -> `Some::class.java`
- replace use/import of anno `groovy.transform.CompileStatic` with Kotlin's analog

## todo requiring more analysis

- translate Groovy's implicit `return`
- "groovy truth" can't be translated straightly
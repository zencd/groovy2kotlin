# groovy2kotlin

An utility converting [Groovy](http://groovy-lang.org/) sources to [Kotlin](https://kotlinlang.org/) language.

Started Nov 2019 by [zencd](https://github.com/zencd) in order to convert a project of mine
because I've not found any existing tools (and it's easy to write one by myself).
The result of translation *isn't 100%* Kotlin-correct usually and user is required to review the result,
but a significant amount of work can be saved for free.

The policy:
- Don't loose any piece of code;
- Produced code could be partially invalid in syntax/linking;
- But it is acceptable since an operator can easily review such errors;
- Silent behavioural changes are highly undesirable;
- But they are still possible because Groovy can do unpredictable things in runtime;
- Nevertheless do the best to emit valid, readable code.

The current implementation's passes:
- One or multiple modules are parsed into AST by the Groovy itself;
- All types (including dependent libs) gets resolved as strictly as possible;
- AST traversed to resolve types of locals and expressions;
- Kotlin code generated.

Feel free to submit a bug, ticket and do all the github things.

## Requirements

- JDK 1.8 (for running the tool)
- Groovy 2.5.8 (for running the tool + input language level)
- Kotlin 1.3 (produced code level)

Others may be supported but was not tested.

## Usage

[See here](USAGE.md)

## Structure

- `Gtk.toKotlinAsSingleString()` - converts a Groovy script text into Kotlin
- `BulkProcessor` - converts a directory recursively
- `GroovyToKotlin` - the translator engine
- `test-data/input-output-tests/` - a set of input/output tests showing how the things gets translated

## Example

![demo comparison](demo2.png)

## Todo

[See here](TODO.md)

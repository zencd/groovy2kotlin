## Todo

- NULL INFERENCE
    - method cannot be found if any actual argument is null
    - detect `a == null` checks in source code
    - ✔ for locals
    - ✔ for fields
    - for a method return value
- RW- & null-inference: if a implicit setter is invoked then apply the inference rules
- Groovy's list expansion
- Fix `val localWithoutInitializer` - var must have a type or be initialized
- Method signature cannot be found with a `null` actual argument - need a custom algo
- Start considering annotations like `@Nullable`
- Convert `"a"` to `'a'` if acceptor's type is `char`
- Special treatment of `asBoolean()`, `expr as boolean`, `(boolean)expr`, `boolean x = expr`, etc in Groovy
- Wildcard expressions: `list*.prop = 123`
- Presence of both field `some` and `getSome()` is not respected by Kotlin (but it's ok in Groovy)
- If there are both field `some` and `getSome()` in Groovy code, the method should be preferred (like Groovy does)
- Groovy scripts (statements outside any classes)
- Allow annotations for everything
- Preserve javadoc (there is a way)
- Translate multi-line strings with respect for the author's formatting, and use `trimIndent()`
- Kotlin prohibits implicit casts like `Int -> Long`
- Groovy allows implicit casts like `String s = 1L`
- `static` can't be applied to static inner classes
- Groovy's special use of bitwise operators on ~~lists~~, streams, etc
- Translate Groovy's implicit `return` (solved for certain cases)
- Start checking for custom type-into-type converters defined in user classes
- Mappings:
    - `String.execute()`
    - `File.text`
    - `File.eachLine()` → `forEachLine`
    - `List.eachWithIndex()` → `forEachIndexed` (params swapped!)
    - `List.grep()`
- Non urgent things:
    - `@Deprecated` → `@kotlin.Deprecated(message)`
    - `a && b && c` leads to correct but complicated code gen
- Add tests:
    - `list << item`
    - `String.toByteArray`
    - `File.size()`
    - `@Override` not translated
    - `File.getText`
    - `const val`
    - `List.size() -> size`
    - `List.every() -> all`

## What's done

✔ Classes ✔ Interfaces
✔ Inheritance
✔ Overriding
✔ Anonymous classes 
✔ Inner classes ✔ Nested classes
✔ Annotations
✔ Range expression
✔ In-string expressions
✔ Bitwise expressions
✔ Exceptions
✔ `in` ✔ `==~` ✔ `=~` ✔ `is` ✔ `<=>`
✔ Arrays
✔ Local vars
✔ Multiple value assignment
✔ `/.+/` regexps
✔ `if/else` ✔ `switch` ✔ `while` ✔ `for in` ✔ `for(;;)`
✔ `break` ✔ `continue`
✔ Loop labels
✔ `try/catch`
✔ `throws`
✔ Standard methods
✔ `const`
✔ Groovy truth
✔ Groovy's property-style method access


## Progress on a project

    2019-11-10 - 4734 Kotlin errors
    2019-11-12 - 848
    2019-11-15 - 714
    2019-11-16 - 619
    2019-11-17 - 526
    2019-11-19 - 487
    2019-11-21 - 443
    2019-11-24 - 419
    2019-11-25 - 387
    2019-11-28 - 367
    2019-11-29 - 349

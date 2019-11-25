## Todo

- Use `true` not `return true` within closures
- Convert `"a"` to `'a'` if acceptor's type is `char`
- Special treatment of `expr as boolean`, `(boolean)expr`, `boolean x = expr`  in Groovy
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
- Check a field is never rewritten thru code, and mark it as `val` then
- Groovy's special use of bitwise operators on ~~lists~~, streams, etc
- Translate Groovy's implicit `return` (solved for certain cases)
- Groovy truth: check for presence of `asBoolean()` in user classes
- Start checking for custom type-into-type converters defined in user classes
- Mappings:
    - `String.execute()`
    - `File.text`
    - `File.eachLine()` → `forEachLine`
    - `List.eachWithIndex()` → `forEachIndexed` (params swapped!)
    - `List.grep()`
- Non urgent things:
    - `@Deprecated` -> `@kotlin.Deprecated(message)`
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
    2019-11-25 - 408

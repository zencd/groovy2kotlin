## Todo

- Convert `"a"` to `'a'` if acceptor's type is `char`
- Fix `constructors.txt`
- Logical NOT, double NOT
- Special treatment of `expr as boolean`, `(boolean)expr`, `boolean x = expr`  in Groovy
- Kotlin prohibits implicit casts like `Int -> Long`
- Wildcard expressions: `list*.prop = 123`
- Presence of both field `some` and `getSome()` is not respected by Kotlin (but it's ok in Groovy)
- If there are both field `some` and `getSome()` in Groovy code, the method should be preferred (like Groovy does)
- Groovy scripts (statements outside any classes)
- Allow annotations for everything
- Preserve javadoc (there is a way)
- Replace use/import of anno `groovy.transform.CompileStatic` with Kotlin's analog
- Probably translate `def` → `Any` in formal params, not `Object`
- Groovy allows implicit conversions like `String s = 1L`
- Groovy comparison operator `<=>`
- `static` can't be applied to static inner classes
- Check a field is never rewritten thru code, and mark it as `val` then
- Groovy's special use of bitwise operators on lists, streams, etc
- Translate Groovy's implicit `return` (solved for certain cases)
- Implement "Groovy truth" behaviour for:
    - List and Map ancestors
    - Object
    - Check for presence of `asBoolean()`
- Mappings:
    - `String.length()` → `length` attr
    - `String.execute()`
    - `File.text`
    - `File.eachLine()` → `forEachLine`
    - `List.eachWithIndex()` → `forEachIndexed` (params swapped!)
    - `List.grep()`
- Optional:
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

## Some history

✔ Classes ✔ Interfaces
✔ Anonymous classes 
✔ Inner classes ✔ Nested classes
✔ Inheritance
✔ Annotations
✔ Range expression
✔ In-string expressions
✔ Bitwise expressions
✔ Exceptions
✔ `in`
✔ `==~`
✔ `=~`
✔ `is`
✔ Arrays
✔ Local vars
✔ Multiple value assignment
✔ `/.+/` regexps
✔ `if`
✔ `while`
✔ `for(;;)`
✔ `break` ✔ `continue`
✔ Loop labels
✔ `throws`
✔ Overriding
✔ Standard methods
✔ `const` for constants
✔ Groovy truth (partial support yet)

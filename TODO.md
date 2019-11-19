## Todo 1

- Logical NOT, double NOT
- Special treatment of `x as boolean` etc in Groovy
- Kotlin prohibits implicit casts like `Int -> Long`
- Wildcard expressions: `list*.prop = 123`
- Presence of both field `some` and `getSome()` is not respected by Kotlin (but it's ok in Groovy)
- If there are both field `some` and `getSome()` in Groovy code, the method should be preferred (like Groovy does)
- Operator `input =~ regex` -> `regex.toRegex().matchEntire(input)`
- Groovy scripts (statements outside any classes)
- Allow annotations for everything
- Do import static things like this: `import pack.Classe.Companion.staticMethod`
- Preserve javadoc (there is a way)
- Replace use/import of anno `groovy.transform.CompileStatic` with Kotlin's analog
- Probably translate `def` → `Any` in formal params, not `Object`
- Groovy allows implicit conversions like `String s = 1L`
- Groovy comparison operator `<=>`
- static inner classes can't be translated with modifier `static`
- `List` mapping:
    - `eachWithIndex` → `forEachIndexed` (params swapped!)
    - `grep` → `?`
- `File` mapping:
    - `size()` → `length()`
    - `File.text`
    - `eachLine` → `forEachLine`
- `String` mapping:
    - `length()` → `length` attr
    - `String.execute()`
- `Number` mapping:
    - `intValue()` → `.toInt()` etc
- Optional:
    - `Deprecated` -> `@kotlin.Deprecated(message)`
    - Add `const` to `val FLAGS: Int = 11`
- Add tests:
    - `list << item`
    - `String.toByteArray`
    - `File.size()`
    - `@Override` not translated
    - `File.getText`
    - `const val`
    - `List.size() -> size`
    - `List.every() -> all`

## Todo 2

- Check a field is never rewritten thru code, and mark it as `val` then
- Groovy's special use of bitwise operators on lists, streams, etc
- Translate Groovy's implicit `return` (solved for certain cases)
- Implement "Groovy truth" behaviour for:
    - List and Map ancestors
    - Object

## Some history

✔ Classes ✔ Interfaces
✔ Anonymous classes 
✔ Inner classes ✔ Nested classes
✔ Inheritance
✔ Annotations
✔ Range expression
✔ In-string expressions
✔ Bitwise expressions
✔ `in`
✔ `==~`
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

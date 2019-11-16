# Usage

## Build

    git clone "https://github.com/zencd/groovy2kotlin"
    cd groovy2kotlin
    gradlew clean test jar

## Running

The tool performs type checking so you are required to supply:

- Your Groovy project's *valid* source files
- The `groovy2kotlin.jar` or corresponding classes
- All dependencies of your Groovy project

### Option 1

Personally I'd just fetch the code into the Groovy project intended for the conversion
and call the following code from it. Dependencies are supposed to be pre-resolved,
so this is the simplest way.

    gtk.BulkProcessor.process('/my/project/src/main/groovy', '/dest')

### Option 2

It is possible also to supply the `groovy2kotlin.jar` as classpath and load
project's dependencies programmatically:

    import gtk.BulkProcessor
    
    static ClassLoader makeUrlClassLoader(ClassLoader parent, List<String> jarPaths) {
        def urls = jarPaths.collect { new File(it).toURI().toURL() } as URL[]
        return new URLClassLoader(urls, parent)
    }

    void convert_a_project() {
        def jarPaths = [
                'C:/Users/user/.m2/repository/*/logback-classic-1.1.2.jar',
                'C:/Users/user/.m2/repository/*/mail-1.4.jar',
        ]
        def cl = makeUrlClassLoader(Your.class.classLoader, jarPaths)
        BulkProcessor.process('/my/project/src/main/groovy', '/dest', cl)
    }

Don't forget to *exclude* the `groovy-all` jar!

### Option 3: the command line

From command line execute:

    groovy
      -cp build/libs/groovy2kotlin-0.0.1.jar:<<EVERY-RUNTIME-DEPENDENCY.jar>>
      -e "gtk.BulkProcessor.process('/my/project/src/main/groovy', '/dest')"

Since the tool performs type checking, you will need to supply not only the Groovy
sources but the project's runtime dependency jars also. You can supply them as
a `-cp` argument joined by `:` on unixes and `;` on Windows.

### Listing all the jar files

To help yourself listing the jars, and if your project is gradle-managed,
you can add this to `build.gradle`:

    task(listRuntimeJars) {
	    configurations.runtimeClasspath.files.forEach {
	        print "${File.pathSeparator}${it.absolutePath}"
        }
    }

And execute:

    gradlew listRuntimeJars

This gonna list all the jars required for running your app in runtime.
Pass them as the `-cp` arg above.

And don't forget to *exclude* the `groovy-all` jar!

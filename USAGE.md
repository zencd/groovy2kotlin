# Usage

## Build

    git clone "https://github.com/zencd/groovy2kotlin"
    cd groovy2kotlin
    gradlew clean test jar

## Running

From command line execute:

    groovy
      -cp build/libs/groovy2kotlin-0.0.1.jar:<<EVERY-RUNTIME-DEPENDENCY.jar>>
      -e "gtk.BulkProcessor.process('/groovy-project/src/main/groovy', '/output')"

Since the tool performs type checking, you will need to supply not only the Groovy
sources but the project's runtime dependency jars also. You can supply them as
a `-cp` argument joined by `:` on unixes and `;` on Windows.

## Listing all the jar files

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

Don't forget to *exclude* the 'groovy-all' jar!

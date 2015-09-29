## fluent-r

The Fluent R library offers a simple solution for R analytics integration within any Java, Groovy, Scala or Clojure application. The library exposes a simple and intuitive natural language interface called the Fluent R DSL. This DSL supports the declaration and execution of R analytics services offered by popular open source R integration servers, including [DeployR](http://deployr.revolutionanalytics.com) and [OpenCPU](http://opencpu.org).

For a complete description of the Fluent R DSLs currently supported by the library see the following grammar specifications:

- [DeployR Fluent R DSL](dsl/deployr/README.md)
- [OpenCPU Fluent R DSL](dsl/opencpu/README.md)

### Gradle Dependency

```
compile 'io.onetapbeyond:fluent-r:1.0'
```

### Maven Dependency

```
<dependency>
  <groupId>io.onetapbeyond</groupId>
  <artifactId>fluent-r</artifactId>
  <version>1.0</version>
</dependency>
```

### Usage

When working with this library the programming model is simple:

1. Use the Fluent R DSL to define the analytics requirements for your application.
2. Use the Fluent R task builder to invoke the DSL within your application.
3. Use the Fluent R task result data as needed directly within your application.

The Fluent R library Javadoc is available [here](http://www.javadoc.io/doc/io.onetapbeyond/fluent-r/). The following examples are provided to demonstrate basic usage.

### DeployR Analytics Integration Example

**[1] Use the Fluent R DSL to define your R analytics requirements.**

The following example DSL requests the execution of the `Histogram of Auto Sales` repository-managed script owned by `testuser` followed by the retrieval of the `histogram.png` file generated into the working directory (wd) and the `cars` and `trucks` objects generated into the workspace by that script:

```
execute 'Histogram of Auto Sales' from 'root' by 'testuser'
fetch 'histogram.png' from wd
fetch 'cars', 'trucks' from workspace
```

**[2] Use the Fluent R task builder to invoke the DSL within your application.**

Using the task builder the application integration code simply identifies the DeployR server endpoint, and the Fluent R DSL to be executed:

```
import static io.onetapbeyond.fluent.r.DeployRTaskBuilder.*

FluentResult fluentResult = fluentTask("http://localhost:7400/deployr")
						    .stream(fluentDSL)
						    .execute()
```

The `fluentDSL` parameter value in this example represents a custom DSL. The DSL may be read from a `String` but the library also supports *streaming* custom DSL definitions from numerous local and remote sources, including `URL`, `URI`, `File`, `Path`, `InputStream`, `Reader`.

**[3] Use the Fluent R task result data as needed directly within your application.**

The *FluentResult* returned by the task execution provides easy access to all requested data. The available data correspond to your use of *fetch* verbs in your DSL, for example:

```
List<URL> files = fluentResult.files
Map<String, RData> objects = fluentResult.objects
RData cars = objects.get("cars")
RData trucks = objects.get("trucks")
```

### OpenCPU Analytics Integration Example

**[1] Use the Fluent R DSL to define your R analytics requirements.**

The following example DSL requests the execution of the `rnorm` function within the `stats` package:

```
execute 'rnorm' from 'stats'
```

**[2] Use the Fluent R task builder to invoke the DSL within your application.**

Using the task builder the application integration code simply identifies the OpenCPU server endpoint, the Fluent R DSL to be executed, and data input values to be sent on the function execution:

```
import static io.onetapbeyond.fluent.r.OpenCPUTaskBuilder.*

FluentResult fluentResult = fluentTask("http://public.opencpu.org/ocpu/")
						    .stream(fluentDSL)
						    .send([ "n" : 11, "mean" : 5])
						    .execute()
```

The `fluentDSL` parameter value in this example represents a custom DSL. The DSL may be read from a `String` but the library also supports *streaming* custom DSL definitions from numerous local and remote sources, including `URL`, `URI`, `File`, `Path`, `InputStream`, `Reader`.

**[3] Use the Fluent R task result data as needed directly within your application.**

The *FluentResult* returned by the task execution provides easy access to all requested data. If your DSL makes an R function call then the return value of the function is automatically made available in the Map, for example:

```
List rnorm = fluentResult.objects.get("rnorm")
```

If your DSL makes an R script call the available console, plot, wd, and workspace data in the *FluentResult* will correspond to your use of *fetch* verbs in your DSL.

### Working with Encrypted R Integration Server Connections

If the R integration server where your tasks are executing requires
you to use an encrypted HTTPS connection then you must configure the
JVM security environment where your application is running with
appropriate key and trust stores. See the following Oracle
[documentation](http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html) for details.

However, for dev and test purposes a convenience method is provided on
*FluentTask* that temporarily allows connections to R Integration
servers without valid certificates in your local configuration. For example:

```
FluentResult fluentResult = fluentTask("https://public.opencpu.org/ocpu/")
						    .stream(fluentDSL)
						    .send([ "n" : 11, "mean" : 5])
						    .blindTrust(true)
						    .execute()
```

This method is called *blind trust* for a reason. It is strongly recommended that this setting should be disabled in production environments.

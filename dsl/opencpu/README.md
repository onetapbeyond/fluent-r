# OpenCPU Fluent R DSL

The OpenCPU Fluent R DSL supports the declaration and execution
of OpenCPU R analytics services using a simple and intuitive
natural language interface.

The complete DSL grammar is defined as follows: 

```
execute FUNCTION | SCRIPT from PACKAGE [on cran | github | opencpu] [by USER]
fetch console | plots
fetch FILENAME | OBJECTNAME from wd | workspace 
```

All `uppercase` words in the grammar definition above represent variable
placeholders that require substitution within your custom DSL scripts.
All `lowercase` words are *keywords* in the grammar itself. Numerous
examples demonstrating usage of the DSL follow.

The OpenCPU Fluent R grammar defines just 2 verbs: *execute* and *fetch*.

## Verb: execute

The *execute* verb supports the exectuion of `functions`
and `scripts` within a named R PACKAGE. The named package can reside
on *CRAN*, on *github* or on the default location, the OpenCPU
server library.

The USER variable placeholder can be used to identify the [opencpu, github]
package owner.

For example, to request the execution of the `rnorm` function from the
`stats` package on the OpenCPU server:

```
execute 'rnorm' from 'stats'
```

Alternatively, to request the execution of the `geodistance` function
from the `dpu.mobility` R package available on the `openmhealth`
github repository:

```
execute 'geodistance' from 'dpu.mobility' on github by 'openmhealth'
```

## Verb: fetch

The *fetch* verb supports the retrieval of data following the
execution of a task. The following data types can be fetched:

1. R console output
2. R graphics device plots
3. R workspace objects
4. R working directory (wd) files

For example, to request the retrieval of the `df` and `age`
objects from the workspace along with the R console output following
the exectuion of the task:

```
fetch 'df', 'age' from workspace
fetch console
```
All data retrieved following the execution of a task is returned on an
instance of *io.onetapbeyond.fluent.r.FluentResult*.

## DSL: Sample

The structure of a complete OpenCPU Fluent R DSL may look as follows:

```
execute 'plot' from 'graphics'
fetch plots
```

## Sending Data Inputs on Task

If the execution of your custom DSL requires input data to be sent on the
execution by your application, for example input values for `n`
and `mean` on the *rnorm(n, mean)* function call, then use
the *io.onetapbeyond.fluent.r.tasks.FluentTask#send* methods provided.


# DeployR Fluent R DSL

The DeployR Fluent R DSL supports the declaration and execution
of DeployR R analytics services using a simple and intuitive
natural language interface.

The complete DSL grammar is defined as follows:

```
load FILENAME into wd | workspace from DIRECTORY by AUTHOR
execute FILENAME from DIRECTORY by AUTHOR  
fetch console | plots  
fetch FILENAME | OBJECTNAME from wd | workspace
```

All `uppercase` words in the grammar definition above represent variable
placeholders that require substitution within your custom DSL scripts.
All `lowercase` words are *keywords* in the grammar itself. Numerous
examples demonstrating usage of the DSL follow.

The DeployR Fluent R grammar defines just 3 verbs: *load*,
*execute* and *fetch*.

## Verb: load

The *load* verb supports the loading of repository-managed
`files` and `binary R objects` into the working directory
and workspace of the task respectively, prior to execution. Your custom
DSL scripts can specify one or more *load* statements as needed.
If you need to load multiple files or binary R objects
from the same repository-managed directory you can use a comma-separated
list of file names on a single *load* statement.

For example, to request the loading of a data file (`accounts.csv`),
and two binary R objects (`savings.rData` and `checking.rData`) into
the task's working directory (wd) and workspace respectively:

```
load "accounts.csv" into wd from "banking" by "datamgr"
load "savings.rData", "checking.rData" into workspace from "banking" by "datamgr"
```

## Verb: execute

The *execute* verb supports the exectuion of one or more 
repository-managed scripts, each identified by `filename`,
`directory` and `author`. Your custom DSL scripts can specify one more
*execute* statements as needed. When more than one *execute*
statement appears, the scripts are executed as a sequential chain on a
single R session in the order specified in the DSL.

For example, to request the execution of the `evalRisk.R` script
found in the `fraud-score` directory owned by `fraudmgr`:

```
execute "evalRisk.R" from "fraud-score" by "fraudmgr"
```

## Verb: fetch

The *fetch* verb supports the retrieval of data following the execution
of a task. The following data types can be fetched:

1. R console output
2. R graphics device plots
3. R workspace objects
4. R working directory (wd) files

For example, to request the retrieval of the `score` and `risk`
objects from the workspace along with the R console output following
the exectuion of the task:

```
fetch "score", "risk" from workspace
fetch console
```

All data retrieved following the execution of a task is returned on an
instance of link *io.onetapbeyond.fluent.r.FluentResult*.

## DSL: Sample

The structure of a complete DeployR Fluent R DSL may look as follows:

```
load "accounts.csv" into wd from "banking" by "datamgr"
load "savings.rData", "checking.rData" into workspace from "banking" by "datamgr"
execute "evalRisk.R" from "fraud-score" by "fraudmgr"
fetch "score", "risk" from workspace
fetch console
```

## Sending Data Inputs on a Task

If the execution of your custom DSL requires input data to be sent on the
execution by your application, for example input values used when
scoring the risk of fraud on the `evalRisk.R` script, then use the
*io.onetapbeyond.fluent.r.tasks.FluentTask#send* methods provided.


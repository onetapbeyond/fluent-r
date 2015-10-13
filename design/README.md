## Developing Zero-Code R Integration APIs

The fluent-r library offers a simple solution for R analytics integration inside any Java, Groovy, Scala or Clojure application. But the full potential of the library is best understood when you recognize that each individual DSL you define is in effect defining an `API service` and any collection of DSLs you define is therefore defining a custom `R Integration API`.

### Zero-Code

Thanks to the [Fluent R DSL](../README.md) natural language syntax, defining a DSL is simple, for example:

```
execute "score.R" from "models" by "fraudteam"
fetch "score" from workspace
```

Not only is this simple, but whether you define one, tens or even hundreds of DSLs you can do so withing having to write a single line of application code. This allows you to focus on defining a clean `R Integration API` determined solely by your R analytics needs.

### Zero-Complexity Integration

Within any application you may have numerous integration points in need of R analytics services. Often each integration point requires custom code to communicate with specific APIs on the R integration server. Typically the greater the number of integration points, the greater the complexity of the code base for your integration solution. With the fluent-r library, these complexities fade away.

Having developed your set of DSLs into an `R Integration API`, the fluent-r library offers a compellingly simple integration solution, a single API call:

```
FluentResult result = fluentTask(RServer)
					  .stream(fluentDSL)
					  .send(inputs)
					  .execute();
```

Regardless of how many DSLs you've defined for your `R Integration API`, all services on that API are invoked using this single `fluentTask()` method call. As a further convenience within your own application you may want to wrap the `fluentTask()` call within a custom method, for example:

```
FluentResult executeR(String fluentDSL, Map inputs) {
	return fluentTask(RServer).stream(fluentDSL).send(inputs).execute();
}
```

Then executing any service on your integration API is as simple as calling `executeR(fluentDSL, inputs)`.

### Zero-Complexity Maintenance

Over time your `R Integration API` may need to evolve, for example, you may need to update existing DSL service definitions to take advantage of new models or scoring functions made available on your R integration server. To do so simply requires changes to one or more of the natural language DSLs that make up your `R Integration API`.

If your DSLs are maintained at a location external to your live application bundle, for example on `github` or some other URL or file addressable location, changes to the `R Integration API` could even be applied as `hot-fixes` while your application was live, without service interruption.

### Zero-Code In Practice

Consider a real-time scoring application that relies on R analytics to score high-volume banking transactions for potential fraud. Based on the analysis of historical transaction data and extensive modeling by your data scientists, your team has decided to deploy three distinct fraud models for `checking`, `savings` and `investment` accounts respectively.

Given this approach, a complete DSL-powered `Fraud Integration API` for your application may look as follows:

```
// 1. Checking Account Fraud Scoring Fluent DSL
execute 'scoreChecking.R' from 'fraud-models' by 'fraudteam'
fetch 'score' from workspace
```

```
// 2. Savings Account Fraud Scoring Fluent DSL
execute 'scoreSavings.R' from 'fraud-models' by 'fraudteam'
fetch 'score' from workspace
```

```
// 3. Investment Account Fraud Scoring Fluent DSL
execute 'scoreInvestment.R' from 'fraud-models' by 'fraudteam'
fetch 'score' from workspace
```

Without writing a single line of application code your team has defined a clean `Fraud Integration API` determined solely by the R analytics needs of your fraud-scoring application.

Of course, over time, things can change. Having already deployed the original `Fraud Integration API` into production your team may decide at some later date that the API needs to be extended to support custom fraud detection on special `student` accounts.

To extend the API you can simply define a new DSL:

```
// 4. Student Account Fraud Scoring Fluent DSL
execute 'scoreStudent.R' from 'fraud-models' by 'fraudteam'
fetch 'score' from workspace
```

With this additional DSL deployed the `Fraud Integration API` is extended with trivial effort. And herein lies the power and potential of the fluent-r library. You can check out sample DSLs and integration code for *DeployR* and *OpenCPU* are available as gist repositories on github [here](https://gist.github.com/search?utf8=%E2%9C%93&q=fluent-r). 

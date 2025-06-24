# Running the Analysis via a CLI
The Data Flow Analysis can also be run using a CLI interface.  
For that, we provide two Binaries on our [Download Page](/download/).

The analysis can be run the `DFDAnalysisCLI` or `PCMAnalysisCLI` respectively, depending on which model you are using.

## Command Format
When no parameters are provided to the CLI, the CLI asks interactively for the required file paths and other inputs to run the analysis.

Additionally, the CLI can be run with the paths to the model files (both absoulute and relative) and the [DSL Constraint](/wiki/dsl/) that should be analyzed.

The constraints passed to the analysis can either be defined in a `.dfadsl` plain text file, or directly as a DSL Constraint String.

```
DFDAnalysisCLI [<.dataflowdiagram> <.datadictionary> <.dfadsl|DSL Constraint String>]
```

```
PCMAnalysisCLI [<.usagemodel> <.allocation> <.nodecharacteristics> <.dfadsl|DSL Constraint String>]
```

### Usage Examples 
::: tip Running the DFD analysis using the interactive CLI interface 
The interactive CLI for the DFD analysis can be run with the following command: 
```
DFDAnalysisCLI
```
:::

::: tip Running the DFD analysis using paths to the model files and a constraint passed as a parameter
Running the DFD analysis with models located in the current working directory and a constraint passed directly as a parameter can be done like this:
```
DFDAnalysisCLI model.dataflowdiagram model.datadictionary "data Sensitivity.Personal neverFlows vertex Location.nonEU"
```
:::

::: tip Running the PCM analysis with model files and a constraint file
Running the PCM analysis can be run with model files and a `.dfadsl` file using the following command:
```
PCMAnalysisCLI model.usagemodel model.allocation model.nodecharacteristics model.dfadsl
```
:::

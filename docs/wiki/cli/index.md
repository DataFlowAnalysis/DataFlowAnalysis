# Running the Analysis via a CLI
The Data Flow Analysis can also be run using a CLI interface.  
For that, run the `DFDAnalysisCLI` or `PCMAnalysisCLI` respectively. 
The command line interface can be either called with path to the model files and a constraint or interactively, when no parameters are passed. 
The constraints passed to the analysis can either be defined in a `.dfadsl` plain text file, or directly as a DSL Constraint String.

```
DFDAnalysisCLI [<.dataflowdiagram> <.datadictionary> <.dfadsl|DSL Constraint String>]
```

```
PCMAnalysisCLI [<.usagemodel> <.allocation> <.nodecharacteristics> <.dfadsl|DSL Constraint String>]
```

### Usage Examples 
Running the DFDAnalysisCLI interactively
```
DFDAnalysisCLI
```

Running the DFDAnalysisCLI with models located in the current working directory and a DSL Constraint String
```
DFDAnalysisCLI model.dataflowdiagram model.datadictionary "data Sensitivity.Personal neverFlows vertex Location.nonEU"
```

Running the PCMAnalysisCLI with models and a `.dfadsl` file: 
```
PCMAnalysisCLI model.usagemodel model.allocation model.nodecharacteristics model.dfadsl
```

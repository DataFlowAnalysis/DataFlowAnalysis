# DataFlowAnalysis

This repository contains an analysis to extract dataflows from a palladio modelling project,
analyze their data characteristics and formulate constraints to check if definied bounds are respected by the model.
For more information visit the [official page](https://dataflowanalysis.org/) of the project.

## Installation

- Install the product from our [updatesite](https://updatesite.palladio-simulator.com/DataFlowAnalysis/product/nightly/) 
- Clone this repository
- Import the projects in this repository 

## Usage

First step of using the project is to create a modelling project in Eclipse.
Furthermore, the Activator class,
an example found at the project tests/org.dataflowanalysis.analysis.tests,
is needed to load the models.
It determines the base location of the modelling project.
The `PROJECT_NAME` in the example code is determined by the project name
(e.g. org.dataflowanalysis.analysis.tests) of the modelling project that should be analyzed.
The `USAGE_MODEL_PATH`, `ALLOCATION_MODEL_PATH` and `NODE_MODEL_PATH` describe the path of to the usage, allocation and node characteristics model relative to the root of the modelling project.

Currently, the analysis only supports analyzing a project which has a activator.
Should you wish to use models outside of a project, you must use relative paths from an project with an Activator class.
This is step is required, as standalone initialization of the analysis requires an eclipse project to work.

A basic analysis can be executed with the following example:

```java
public static void main(String[] args) {
    PCMDataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
            .modelProjectName("<PROJECT_NAME>")
            .usePluginActivator(Activator.class)
            .useUsageModel("<USAGE_MODEL_PATH>")
            .useAllocationModel("<ALLOCATION_MODEL_PATH>")
            .useNodeCharacteristicsModel("<NODE_MODEL_PATH>")
            .build();

    analysis.setLoggerLevel(Level.TRACE); // Set desired logger level. Level.TRACE provides additional propagation
    // Information
    analysis.initializeAnalysis();

    PCMFlowGraphCollection flowGraph = analysis.findFlowGraphs();
    flowGraph.evaluate();

    for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
        List<? extends AbstractVertex<?>> violations = analysis.queryDataFlow(transposeFlowGraph, it -> false // Constraint goes here, return true, if
                                                                                                          // constraint is violated
        );
    }
}
```

Additional examples for the TravelPlanner, InternationalOnlineShop and BranchingOnlineShop can be found in the tests and testmodel projects, that can be found at the tests folder in the root of the project.

## Analysis CLI 
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

# Palladio-Addons-DataFlowConfidentiality-Analysis

This repository contains an analysis to extract dataflows from a palladio modelling project,
analyze their data characteristics and formulate constraints to check if definied bounds are respected by the model.
For more information visit the [official page](https://fluidtrust.ipd.kit.edu/home/) of the project.

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
public class Main {
  public static void main(String[] args) {
      PCMDataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder()
	        .standalone()
	        .modelProjectName("<PROJECT_NAME>")
	        .usePluginActivator(Activator.class)
	        .useUsageModel("<USAGE_MODEL_PATH>")
	        .useAllocationModel("<ALLOCATION_MODEL_PATH>")
	        .useNodeCharacteristicsModel("<NODE_MODEL_PATH>")
	        .build(); 

      analysis.setLoggerLevel(Level.TRACE); // Set desired logger level. Level.TRACE provides additional propagation Information
      analysis.initializeAnalysis();

      PCMFlowGraph flowGraph = analysis.findFlowGraph();

      PCMFlowGraph propagatedFlowGraph = analysis.evaluateFlowGraph(flowGraph);
	    
      for(AbstractPartialFlowGraph actionSequence : propagatedFlowGraph.getPartialFlowGraphs()) {
	       List<AbstractVertex<?>> violations = analysis.queryDataFlow(actionSequence,
	       it -> false // Constraint goes here, return true, if constraint is violated
	     );
	   }
    }
}
```

Additional examples for the TravelPlanner, InternationalOnlineShop and BranchingOnlineShop can be found in the tests and testmodel projects, that can be found at the tests folder in the root of the project.

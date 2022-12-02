# Palladio-Addons-DataFlowConfidentiality-Analysis

This repository contains an analysis to extract dataflows from a palladio modelling project, analyze their data characteristics and formulate constraints to check if definied bounds are respected by the model.
For more information visit the [official page](https://fluidtrust.ipd.kit.edu/home/) of the project.

## Installation
- Install the version 2022-09 of the Eclipse Modelling Tools from the [official site](https://www.eclipse.org/downloads/packages/)
- Clone this repository
- Import the dependencies.p2f file into Eclipse to install the dependencies of the project. This is achieved by going to File->Import->General->Install from File
- Import the projects in the bundles folder into Eclipse

## Usage
First step of using the project is to create a modelling project in Eclipse.
Furthermore, the Activator class, found at the project tests/org.palladiosimulator.dataflow.confidentiality.analysis.tests, is needed to load the model placed in the modelling project.
The `projectName` in the example code is determined by the project name of the modelling project.
The `usageModelPath` and `allocationModelPath` describes the path of the usage and allocation model relative to the root of the modelling project

A basic analysis can be executed with the following example:

```java
public class Main {
  public static void main(String[] args) {
    var projectName = "name.of.the.model.project";
    var usageModelPath = Paths.get("PATH", "TO", "THE", "USAGE", "MODEL");
    var allocationModelPath = Paths.get("PATH", "TO", "THE", "ALLOCATION", "MODEL");
    StandalonePCMDataFlowConfidentialtyAnalysis analysis = new StandalonePCMDataFlowConfidentialtyAnalysis(projectName, Activator.class, usageModelPath, allocationModelPath);

    analysis.initializeAnalysis();

    List<DataFlowVariable> actionSequences = analysis.findAllSequences();

    List<DataFlowVariable> propagationResult = analysis.evaluateDataFlows(actionSequences);

    List<DataFlowVariables> violation = analyis.queryDataFlow(actionSequences, 
      it -> false // Constraint goes here, return true, if constraint is violated
    );
  }
}
```

Additional examples for the TravelPlanner, InternationalOnlineShop and BranchingOnlineShop can be found in the tests and testmodel projects, that can be found at the tests folder in the root of the project.

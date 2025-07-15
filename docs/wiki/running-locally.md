# Running the Analysis Locally using our Java API
First step of using the project is to create a modeling project in Eclipse.
Furthermore, the Activator class,
an example found at the project tests/org.dataflowanalysis.analysis.tests,
is needed to load the models.
It determines the base location of the modeling project.
The `PROJECT_NAME` in the example code is determined by the project name
(e.g. org.dataflowanalysis.analysis.tests) of the modeling project that should be analyzed.
The `USAGE_MODEL_PATH`, `ALLOCATION_MODEL_PATH` and `NODE_MODEL_PATH` describe the path of to the usage, allocation and node characteristics model relative to the root of the modeling project.

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

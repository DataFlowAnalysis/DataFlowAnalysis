# Running the Analysis Locally using our Java API
## Initializing the Analysis
To use the analysis one needs to use the corresponding Analysis Builder required to run the analysis:
In the instance of [DFD Models](/wiki/dfd) one should use the `DFDDataFlowAnalysisBuilder` class and for [PCM Models](/wiki/pcm) the `PCMDataFlowConfidentialityAnalysisBuilder`.

Then, one needs to define paths to the corresponding models within a eclipse modeling project.
An example for a eclipse modeling project is the example models bundle at `bundles/org.dataflowanalysis.analysis.examplemodels`

<<< ../../../tests/org.dataflowanalysis.analysis.tests/src/org/dataflowanalysis/analysis/tests/DemoTest.java#init{1-3}

Currently, running the analysis is only possible in standalone mode and is set by calling `standalone()`.
The Activator class is provided to `usePluginAcivator(Class<? extends Plugin>)`, while the models are passed to their corresponding `use` methods.
Finally, a constructed analysis is built from the builder by calling `build()`.
This steps also runs some precursory validation on the provided setup.

<<< ../../../tests/org.dataflowanalysis.analysis.tests/src/org/dataflowanalysis/analysis/tests/DemoTest.java#init{5-11}

The analysis is fully initalized by calling `initializeAnalysis()` on the created analysis object.
If any errors during model loading occur, they will be logged on the command line logger.

<<< ../../../tests/org.dataflowanalysis.analysis.tests/src/org/dataflowanalysis/analysis/tests/DemoTest.java#init{12}

## Writing contstraints and finding violations
<<< ../../../tests/org.dataflowanalysis.analysis.tests/src/org/dataflowanalysis/analysis/tests/DemoTest.java#dsl


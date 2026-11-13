# Setup Guide for Eclipse
## 1. Download the Eclipse Product
Download the Eclipse Product from the [Download Page](/download/) for your platform.
Extract the contained files to a location of your choice.

## 2. Create/Open a workspace
Once Eclipse opens, it will ask you for a location of a workspace.
We recommend setting your workspace to a folder where you intent to place all projects depending on the analysis.
Select this folder in the dialouge as workspace and press the `Launch` button.

## 3. Importing projects
The analysis is built as a plain [Maven](https://maven.apache.org/) multi-module project (JDK 21 is required), so it is imported like any other Maven project.
If you want to work or modify the analysis directly, you first need to clone the analysis repository to your desired location using `git clone`.
Then use the dialouge at `File->Import->Maven->Existing Maven Projects` to import the modules contained in the analysis repository.
Select the folder of the cloned repository as root directory; Eclipse will discover all modules declared in the reactor `pom.xml` files.
Deselect the ones you don't need from the list and keep the following if you need them:

| Module                                            | Description                                       | Recommended?                  |
|--------------------------------------------------- | ------------------------------------------------- | ----------------------------- |
| `bundles/org.dataflowanalysis.analysis`           | Core Functionality of the Analysis                | Required                      |
| `bundles/org.dataflowanalysis.analysis.pcm`       | PCM Functionality of the Analysis                 | Required                      |
| `bundles/org.dataflowanalysis.analysis.dfd`       | DFD Functionality of the Analysis                 | Required                      |
| `tests/org.dataflowanalysis.analysis.tests`       | Tests of the Analysis                             | Required                      |
| `bundles/org.dataflowanalysis.examplemodels`      | Example Models                                    | Required                      |
| `bundles/org.dataflowanalysis.converter`          | Model Converter                                   | Needed for Model Conversion   |
| `tests/org.dataflowanalysis.converter.tests`      | Tests of the Model Converter                      | Needed for Model Conversion   |
| `bundles/org.dataflowanalysis.dfd.datadictionary*`| Data Dictionary Metamodel                         | Only imported when modified   |
| `bundles/org.dataflowanalysis.dfd.dataflowdiagram*`| Data Flow Diagram Metamodel                       | Only imported when modified   |

Note that the artifact IDs of the three core analysis modules are `core`, `pcm` and `dfd` respectively (the `groupId` `org.dataflowanalysis.analysis` already carries the "analysis" prefix), but their folder names keep the longer `org.dataflowanalysis.analysis*` form shown above.

## 4. Testing the Setup
If you have imported the `tests/org.dataflowanalysis.analysis.tests` module, you can run the analysis tests to test your setup.
To do that navigate in the file explorer (typically on the left side) to that module's `src/test/java` folder.
Right click on it and select `Run As->JUnit Test`.
If all tests run sucessfully, your development enviornment is setup correctly!

## 5. Using the Analysis 
Now that you have setup the analysis locally, you can refer to the guide on running the analysis locally [here](/wiki/development/running-locally)

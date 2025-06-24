# Setup Guide for Eclipse
## 1. Download the Eclipse Product
Download the Eclipse Product from the [Download Page](/download/) for your platform.
Extract the contained files to a location of your choice.


## 2. Create/Open a workspace
Once Eclipse opens, it will ask you for a location of a workspace. 
We recommend setting your workspace to a folder where you intent to place all projects depending on the analysis.
Select this folder in the dialouge as workspace and press the `Launch` button.

## 3. Importing projects
If you want to work or modify the analysis directly, you first need to clone the analysis repository to your desired location using `git clone`.
Then use the dialouge at `File->Import->General->Existing Projects into Workspace` to import the plugins contained in the analysis repository.
Select the Folder of the cloned analysis as root directory.
Then use the `Deselect All` button to deselect all found projects and select the following if you need them:
| Project                                           | Description                                       |
|-------------------------------------------------- | ------------------------------------------------- |
| `org.dataflowanalysis.analysis`                   | Core Functionality of the Analysis                |
| `org.dataflowanalysis.analysis.pcm`               | PCM Functionality of the Analysis                 |
| `org.dataflowanalysis.analysis.dfd`               | DFD Functionality of the Analysis                 |
| `org.dataflowanalysis.analysis.tests`             | Tests of the Analysis                             |
| `org.dataflowanalysis.analysis.examplemodels`     | Example Models                                    |
| `org.dataflowanalysis.analysis.converter`         | Model Converter                                   |
| `org.dataflowanalysis.analysis.converter.tests`   | Tests of the Model Converter                      |
| Projects ending with `.feature`                   | Eclipse Feature Plugin                            |
| Projects ending with `.targetplatform`            | Eclipse Targetplatform                            |
| `org.dataflowanalysis.dfd.datadictionary.*`       | Data Dictionary Model, typically not imported     |
| `org.dataflowanalysis.dfd.dataflowdiagram.*`      | Data Flow Diagram Model, typically not imported   |
| Projects eding with `.mwe2`                       | Tests of the Model Converter                      |


## 4. Testing the Setup
If you have imported the `org.dataflowanalysis.analysis.tests` project, you can run the analysis tests to test your setup. 
To do that navigate in the file explorer (typically on the left side) to the `org.dataflowanalysis.analysis.tests` plugin.
Right click on the `src` directory and select `Run As->JUnit Test` (do not select `JUnit Plug-in Test`).
If all tests run sucessfully, your development enviornment is setup correctly!

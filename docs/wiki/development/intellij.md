# Setup Guide for IntelliJ
## 1. Prerequisites
Make sure a JDK 21 is installed and configured in IntelliJ (File->Project Structure->SDKs), and that the [Maven](https://maven.apache.org/) plugin is enabled (it is bundled with IntelliJ by default).

## 2. Cloning and Opening the Project
Clone the analysis repository to your desired location using `git clone`.
Then use `File->Open` and select the cloned repository's root folder (the one containing the top-level `pom.xml`).
IntelliJ will detect the Maven multi-module project and import all modules declared in the reactor `pom.xml` files automatically, resolving their dependencies from Maven Central and GitHub Packages (DataFlowAnalysisVendor, for the P2-only vendor jars).

If you only need a subset of the modules, use the Maven tool window (`View->Tool Windows->Maven`) to unlink the ones you don't need after the initial import.

## 3. Checking the Setup
To check whether you've set up our Data Flow Analysis with IntelliJ correctly, navigate to the module `tests/org.dataflowanalysis.analysis.tests` in the project explorer.
Right-click the `src/test/java` folder and select "Run 'All Tests'".
If the project fails to build, it is likely that the Maven import did not complete successfully; use the Maven tool window's "Reload All Maven Projects" action and check the Build output for errors.

## 4. Using the Analysis
Now that you have set up the analysis locally, you can refer to the guide on running the analysis locally [here](/wiki/development/running-locally).

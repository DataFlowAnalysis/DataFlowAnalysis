## Installation 
The usage of the data flow analysis in IntelliJ is more complicated, so follow these steps carefully:
1. Create a new empty project 

![Creating an empty project](images/intellij-empty-project.png)

2. Place our data flow analysis product (or the contained `plugins` folder) to a well known location 
3. Set the Project SDK in File->Project Structure to your Java SDK and set the language level to Java 17.
4. Using File->Project Structure->Libraries add a new Java library containing the plugin folder of the data flow analysis product 

![Creating an empty Java library](images/intellij-create-library.png)
![The plugin folder location](images/intellij-plugins-location.png)

5. Import all bundles as eclipse projects into IntelliJ using the Eclipse importer.

![Importing a module](images/intellij-import-module.png)
![Using the eclipse importer](images/intellij-import-eclipse.png)

6. If IntelliJ asks for the location of the Eclipse Instance, cancel the dialogue and disregard the following error.

![Canceling the dialogue, when IntelliJ asks for the eclipse instance location](images/intellij-import-cancel.png)

7. Check whether IntelliJ automatically detected the `src` folder of the module as source code. If the bundle contains tests, consider changing it to *test*
8. Clean up the dependency structure of the imported module:

![Location of the dependencies tab](images/intellij-dependencies-tab.png)

9. In the Dependency Tab, select the Project SDK as SDK, and arrange the dependencies using this order from top to bottom: JDK->Source->Module Dependencies->Plugin Library

![Dependency Tab Layout](images/intellij-dependency-structure.png)

10. Modify the default run configurations for JUnit and normal Application runs: This can be done via Edit Configurations->Edit configuration templates

![Editing the run configurations](images/intellij-run-configurations.png)
![Editing the run configuration templates](images/intellij-edit-run-templates.png)

11. Modify the class path to exclude all plugins from the `plugins` folder that are already present in the workspace

![Adding the classpath option](images/intellij-exclude-classpath.png)
![Excluded plugins](images/intellij-excluded-plugins.png)

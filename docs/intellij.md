## Installation 
The usage of the data flow analysis in IntelliJ is more complicated, so follow these steps carefully:
1. Place our data flow analysis product (or the contained `plugins` folder) to a well known location 
2. Create a new IntelliJ IDEA Java project 
3. Using File->Project Structure->Libaries add a new Java library containing the plugin folder of the data flow analysis product 
4. Import all bundles as eclipse projects into IntelliJ. Should IntelliJ ask for the Eclipse Plugin Path, use the `pluigins` folder of the product 
5. Modify the default run configurations for JUnit and normal Application runs: This can be done via Edit Configurations->Edit configuration templates
6. Modify the class path to exclude all plugins from the `plugins` folder that are already present in the workspace

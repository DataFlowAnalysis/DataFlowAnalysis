# Extending the Analysis

There are two levels at which the analysis can be extended:

1. **Using the Java API**: New analyses, constraints, node behaviors, or [converters](/wiki/development/converter) against the existing [DFD](/wiki/dfd/) and [PCM](/wiki/pcm/) metamodels. Covers most extensions and needs nothing beyond a regular Maven dependency; see [Running Locally](/wiki/development/running-locally) for the API itself.
2. **Extending the metamodel**: New EMF model elements that subtype DFD types. Needs a separate Ecore model and genmodel-based code generation, covered below.

## Setting up Maven

Artifacts are published to a static Maven repository:

```xml
<repositories>
	<repository>
		<id>dataflowanalysis</id>
		<name>DataFlowAnalysis Maven Repository</name>
		<url>https://dataflowanalysis.github.io/maven</url>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>org.dataflowanalysis.analysis</groupId>
		<artifactId>core</artifactId>
		<version><!-- see the latest release on GitHub --></version>
	</dependency>
</dependencies>
```

The `groupId` is `org.dataflowanalysis.analysis` for every module. Available artifact IDs:

| Artifact ID | Description |
| --- | --- |
| `core` | Core functionality of the analysis |
| `pcm` | PCM-based analysis support |
| `dfd` | DFD-based analysis support |
| `converter` | Model converter between DFD, PCM, WebDFD, ... |
| `examplemodels` | Example models used in tests and documentation |
| `dfd.dataflowdiagram` | Data Flow Diagram metamodel |
| `dfd.dataflowdiagram.edit` | EMF.Edit providers for the Data Flow Diagram metamodel |
| `dfd.dataflowdiagram.editor` | Eclipse editor for Data Flow Diagram models |
| `dfd.datadictionary` | Data Dictionary metamodel |
| `dfd.datadictionary.edit` | EMF.Edit providers for the Data Dictionary metamodel |
| `dfd.datadictionary.editor` | Eclipse editor for Data Dictionary models |

## Referencing the DFD Ecore Models in a Custom Metamodel

The [Data Flow Diagram](/wiki/dfd/) and Data Dictionary metamodels are plain EMF Ecore models:

| Model | nsURI | Maven Artifact ID |
| --- | --- | --- |
| Data Flow Diagram | `http://dataflowanalysis.org/dfd/dataflowdiagram` | `dfd.dataflowdiagram` |
| Data Dictionary | `http://dataflowanalysis.org/dfd/datadictionary` | `dfd.datadictionary` |

Their `.ecore`/`.genmodel` files are packaged inside those jars under `model/` (e.g. `model/dataflowdiagram.ecore`), alongside the generated Java API. 
Most extensions don't need access to these directly: Using a normal dependency and using the generated classes (`DataFlowDiagramFactory`, `DataDictionaryFactory`, ...) is enough, since loading those classes registers their `EPackage` automatically, so standalone EMF resolution by nsURI works out of the box.

### Extending the metamodel itself

A new Metamodel extension is build as a normal `.ecore`/`.genmodel` in the Eclipse Ecore editor, with `eSuperTypes`/`eType` pointing at the DFD types and `usedGenPackages` pointing at `dataflowdiagram.genmodel`/`datadictionary.genmodel`. 
The editor writes these as relative, workspace-style paths (e.g. `../../../../../org.dataflowanalysis.dfd.dataflowdiagram/model/dataflowdiagram.ecore#//Node`), which do not resolve when modules are build independently.
An external extension project needs to redirect them to the dependency jar at build time:

1. In the `pom.xml` of the bundle, resolve the dependency jar's path with `maven-dependency-plugin`'s `properties` goal, exposing a property like `${org.dataflowanalysis.analysis:dfd.dataflowdiagram:jar}` with the absolute path to the resolved jar.
2. Using the path to the jar from step 1 and its internal path, pass the `archive:file:<jar>!/<path>` URI into a custom MWE2 code-generation workflow. 
They can be launched via exec-maven-plugin's **`java` goal** via its `<arguments>`.
   ```xml
   <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-dependency-plugin</artifactId>
     <executions>
       <execution>
         <id>resolve-dfd-genmodel-jars</id>
         <phase>initialize</phase>
         <goals><goal>properties</goal></goals>
       </execution>
     </executions>
   </plugin>
   <plugin>
     <groupId>org.codehaus.mojo</groupId>
     <artifactId>exec-maven-plugin</artifactId>
     <executions>
       <execution>
         <id>generate-model-code</id>
         <phase>generate-sources</phase>
         <goals><goal>java</goal></goals>
         <configuration>
           <mainClass>org.eclipse.emf.mwe2.launch.runtime.Mwe2Launcher</mainClass>
           <classpathScope>compile</classpathScope>
           <workingDirectory>${maven.multiModuleProjectDirectory}</workingDirectory>
           <arguments>
             <argument>${maven.multiModuleProjectDirectory}/releng/org.myextension.mwe2/workflow/generate-myextension.mwe2</argument>
             <argument>-p</argument>
             <argument>dfdDataFlowDiagramGenModelUri=archive:file:${org.dataflowanalysis.analysis:dfd.dataflowdiagram:jar}!/model/dataflowdiagram.genmodel</argument>
             <argument>-p</argument>
             <argument>dfdDataFlowDiagramEcoreUri=archive:file:${org.dataflowanalysis.analysis:dfd.dataflowdiagram:jar}!/model/dataflowdiagram.ecore</argument>
           </arguments>
         </configuration>
       </execution>
     </executions>
   </plugin>
   ```

3. In the `.mwe2` workflow, map the resolved `platform:/resource/...` URI to that `archive:` URI. 

   ```
   var dfdDataFlowDiagramGenModelUri = "UNSET-dfdDataFlowDiagramGenModelUri"
   var dfdDataFlowDiagramEcoreUri = "UNSET-dfdDataFlowDiagramEcoreUri"

   bean = EclipseRCPSupportingStandaloneSetup {
       ...
       uriMap = {
           from = "platform:/resource/org.dataflowanalysis.dfd.dataflowdiagram/model/dataflowdiagram.genmodel"
           to = dfdDataFlowDiagramGenModelUri
       }
       uriMap = {
           from = "platform:/resource/org.dataflowanalysis.dfd.dataflowdiagram/model/dataflowdiagram.ecore"
           to = dfdDataFlowDiagramEcoreUri
       }

       registerGenModelFile = dfdDataFlowDiagramGenModelUri
   }
   ```

For a usage example see the same mechanism this repository's own bundles use to reference the `identifier.ecore` base model. 
The relevant files can be found in `bundles/org.dataflowanalysis.dfd.*/pom.xml` and the corresponding mwe2 workflow in `releng/org.dataflowanalysis.dfd.mwe2/*.mwe2`.

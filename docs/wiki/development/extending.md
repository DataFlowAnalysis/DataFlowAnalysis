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

A new metamodel extension is built as a normal `.ecore`/`.genmodel` in the Eclipse Ecore editor, with `eSuperTypes`/`eType` pointing at the DFD types and `usedGenPackages` pointing at `dataflowdiagram.genmodel`/`datadictionary.genmodel`.
Code generation runs via a plain `.mwe2` workflow, invoked from Maven through `exec-maven-plugin`'s `java` goal:

```xml
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
        <includePluginDependencies>true</includePluginDependencies>
        <arguments>
          <argument>${project.basedir}/workflow/generate.mwe2</argument>
          <argument>-p</argument>
          <argument>workspaceRoot=${project.basedir}</argument>
        </arguments>
      </configuration>
    </execution>
  </executions>
</plugin>
```

The workflow itself only requires `scanClassPath = true` to discover the referenced metamodels on a plain `org.eclipse.emf.mwe.utils.StandaloneSetup`.
However, it is required to specify a `projectMapping` for the metamodel extension for MWE2 to find the defining plugin of the metamodel extension:

```
var workspaceRoot = "UNSET-workspaceRoot"

Workflow {
	bean = StandaloneSetup {
		scanClassPath = true
		platformUri = workspaceRoot

		projectMapping = { projectName = "org.myextension.metamodel" path = workspaceRoot }
	}

	component = EcoreGenerator {
		generateModel = true
		genModel = "platform:/resource/org.myextension.metamodel/model/myextension.genmodel"
		srcPath = "platform:/resource/org.myextension.metamodel/target/generated-sources/mwe2"
	}
}
```

A concrete example can be found at the [ContextAwareAnalysisFramework](https://github.com/MOCCAVI/ContextAwareAnalysisFramework) repository's `bundles/org.dataflowanalysis.contextawareframework.metamodel*/pom.xml` and `workflow/generate.mwe2` files.
This repository's own bundles (`bundles/org.dataflowanalysis.dfd.*/pom.xml` and `workflow/generate.mwe2`) use the identical pattern for referencing the `identifier.ecore` base model.

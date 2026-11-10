# Vendored dependencies

This directory is a checked-in, file-based Maven repository (declared as a `<repository>` in the
root `pom.xml`) holding 35 artifacts that are **not published to Maven Central**: Palladio PCM/SDQ,
`org.palladiosimulator.commons*`, `tools.mdsd.*` libraries, Eclipse OCL/CDO, and this project's own
`org.dataflowanalysis.pcm.extension.*` bundles. They only exist as P2 update sites.

All artifacts use groupId `org.dataflowanalysis.vendor`, with the artifactId matching the original
OSGi bundle symbolic name and the version matching the original bundle version. Each has a minimal
generated `pom.xml` declaring its own dependencies (translated from the bundle's `MANIFEST.MF`
`Require-Bundle`/`Import-Package` entries), so depending on e.g. `org.palladiosimulator.pcm` pulls in
its full transitive closure automatically.

## Where these came from

Versions are pinned to the Eclipse 2023-03 release train and the specific P2 repositories referenced
by the old `releng/org.dataflowanalysis.analysis.targetplatform` target definition:

| Source | Used for |
|---|---|
| `https://updatesite.palladio-simulator.com/palladio-core-pcm/releases/5.2.2/` | `org.palladiosimulator.pcm` |
| `https://updatesite.palladio-simulator.com/palladio-core-commons/releases/5.2.2/` | `de.uka.ipd.sdq.*`, `org.palladiosimulator.commons*` |
| `https://updatesite.mdsd.tools/library-standaloneinitialization/releases/1.0.0/` | `tools.mdsd.library.standalone.initialization*` |
| `https://updatesite.mdsd.tools/metamodel-modeling-foundations/releases/1.0.0/` | `tools.mdsd.modelingfoundations.identifier*` |
| `https://dataflowanalysis.github.io/updatesite/release/pcm-dataflowanalysis-extension/v5.2.2/` | `org.dataflowanalysis.pcm.extension.*` |
| `https://download.eclipse.org/releases/2023-03/202303151000/` | `org.eclipse.emf.cdo*`, `org.eclipse.ocl*`, `org.eclipse.emf.edit.ui`, `org.eclipse.uml2*`, `org.eclipse.net4j.util`, `org.eclipse.xtend.standalone`, `org.antlr.runtime`, `lpg.runtime.java`, plain `org.eclipse.emf` |

Plain Eclipse Platform/EMF/Xtext/Xtend artifacts that **are** on Maven Central (`org.eclipse.platform`,
`org.eclipse.emf`, `org.eclipse.xtext`, `org.eclipse.xtend`, `org.eclipse.jdt` groups) are declared as
normal dependencies in the root `pom.xml`'s `dependencyManagement` instead of vendored here. Those are
also pinned to the 2023-03 train (see the comment above `emf.ecore.version` etc. in `pom.xml`) — newer
EMF releases break binary compatibility with the vendored PCM/OCL/CDO bytecode (e.g.
`org.eclipse.emf.common.util.CommonUtil.newURL` does not exist in all EMF versions).

## Updating a vendored artifact

1. Download the new bundle jar from its P2 repository's `plugins/` directory
   (`<repo>/plugins/<symbolic-name>_<version>.jar`).
2. Inspect its `META-INF/MANIFEST.MF` for `Require-Bundle`/`Import-Package` and translate each entry
   to either a Maven Central coordinate or another vendored artifact.
3. Install it with:
   ```
   mvn org.apache.maven.plugins:maven-install-plugin:3.1.2:install-file \
     -Dfile=<jar> -DpomFile=<generated-pom> \
     -DlocalRepositoryPath=local-maven-repo -DcreateChecksum=true
   ```
4. Update the version in the root `pom.xml`'s `dependencyManagement` (or the dependent bundle's pom).

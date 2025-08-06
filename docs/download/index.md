---
prev: false
next: false
aside: false
---

# Download

We provide multiple ways to download and use the data flow analysis framework.
The easiest way to get started is downloading our ready-to-use [Eclipse Product](#eclipse-product-recommended).
Alternatively, all major artifacts are available on our [Eclipse Updatesite](#eclipse-updatesite) to be directly installed into the [Eclipse Modeling Framework](https://eclipse.dev/emf/).
Last, if you only want to model and analyze simple data flow diagrams, you can use our [online editor](#online-editor) without any installation required.
Afterwards, visit [Getting Started](/wiki/gettingstarted.md) to learn more about using the analysis.

::: info Current Release
{{releaseInfo}}
:::

## Eclipse Product <Badge type="info">Recommended</Badge>

Our [Eclipse Product](https://updatesite.palladio-simulator.com/DataFlowAnalysis/product/releases/) is a pre-configured Eclipse desktop application with all required dependencies installed to quickly get started.
We provide the Eclipse Product for all major operation systems.

<VPButton text='Windows 10 & 11' href="https://github.com/DataFlowAnalysis/DataFlowAnalysis/releases/download/latest/DataFlowAnalysis.win32.win32.x86_64.zip" />  
<VPButton text='Linux (GTK)' href="https://github.com/DataFlowAnalysis/DataFlowAnalysis/releases/download/latest/DataFlowAnalysis.linux.gtk.x86_64.zip" /> 
<VPButton text='macOS (ARM)' href="https://github.com/DataFlowAnalysis/DataFlowAnalysis/releases/download/latest/DataFlowAnalysis.macosx.cocoa.aarch64.tar.gz" /> 
<VPButton text='macOS (Intel)' href="https://github.com/DataFlowAnalysis/DataFlowAnalysis/releases/download/latest/DataFlowAnalysis.macosx.cocoa.x86_64.zip" /> 
<VPButton text='Plugins (for IntelliJ)' href="https://github.com/DataFlowAnalysis/DataFlowAnalysis/releases/download/latest/DataFlowAnalysis.jars.tar.gz" /> 

## Eclipse Updatesite

We provide an Eclipse Updatesite with all major artifacts ready to be installed in the [Eclipse Modeling Framework](https://eclipse.dev/emf/). The updatesite comprises both the latest release artifacts and a nightly build.

[Release](https://dataflowanalysis.github.io//updatesite/release) (add this URL to your Updatesites in Eclipse):

```
https://dataflowanalysis.github.io//updatesite/release
```

[Nightly](https://dataflowanalysis.github.io//updatesite/nightly) (add this URL to your Updatesites in Eclipse):

```
https://dataflowanalysis.github.io//updatesite/nightly
```

## Online Editor

If you only want to edit and analyze data flow diagrams without further tool support or analysis extensions, just use our online editor, available at https://editor.dataflowanalysis.org/.

<script setup>
import { ref } from 'vue'
import { VPButton } from 'vitepress/theme'
import version from './version.json'

let releaseInfo = ref('The latest released version is available on GitHub.')
console.log(version)
if (version && version.version) {
  console.log(1)
  if (!version.date) {
    releaseInfo = `The latest released version is ${version.version}.`
  } else {
    const date = new Date(version.date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'});
    releaseInfo = `The latest released version ${version.version} was released on ${date}.`
  }
}
</script>

<style module>
a[class*="VPButton"] {
    text-decoration: none !important;
}
</style>

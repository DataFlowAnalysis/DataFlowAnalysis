---
prev: false
next: false
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
We provide it for all major operation systems.

<VPButton text='Windows 10 & 11' href="https://updatesite.palladio-simulator.com/DataFlowAnalysis/product/releases/latest/DataFlowAnalysis.win32.win32.x86_64.zip" />  
<VPButton text='Linux (GTK)' href="https://updatesite.palladio-simulator.com/DataFlowAnalysis/product/releases/latest/DataFlowAnalysis.linux.gtk.x86_64.zip" /> 
<VPButton text='macOS (ARM)' href="https://updatesite.palladio-simulator.com/DataFlowAnalysis/product/releases/latest/DataFlowAnalysis.macosx.cocoa.aarch64.tar.gz" /> 
<VPButton text='macOS (Intel)' href="https://updatesite.palladio-simulator.com/DataFlowAnalysis/product/releases/latest/DataFlowAnalysis.macosx.cocoa.x86_64.zip" /> 

## Eclipse Updatesite

We provide an Eclipse Updatesite with all major artifacts ready to be installed in the [Eclipse Modeling Framework](https://eclipse.dev/emf/). It comprises both the latest release artifacts and a nightly build.

[Release](https://dataflowanalysis.github.io//updatesite/release) (add this URL to your Updatesites in Eclipse):

```
https://dataflowanalysis.github.io//updatesite/release
```

[Nightly](https://dataflowanalysis.github.io//updatesite/nightly) (add this URL to your Updatesites in Eclipse):

```
https://dataflowanalysis.github.io//updatesite/nightly
```

## Online Editor

If you only want to edit and analyze data flow diagrams without further tool support or analysis extensions, just use our online editor at https://editor.dataflowanalysis.org/.

<script setup>
import { ref } from 'vue'
import { VPButton } from 'vitepress/theme'

const releaseInfo = ref('The latest released version is available on GitHub.')
const url = 'https://api.github.com/repos/DataFlowAnalysis/DataFlowAnalysis/releases/latest';

fetch(url).then(response => {
    response.json().then(data => {
    const latestReleaseVersion = data.tag_name;

    const rawDate = new Date(data.published_at);
    const latestReleaseDate = rawDate.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'});

    releaseInfo.value = `The latest released version ${latestReleaseVersion} was released on ${latestReleaseDate}.`
})});
</script>

<style module>
a[class*="VPButton"] {
    text-decoration: none !important;
}
</style>
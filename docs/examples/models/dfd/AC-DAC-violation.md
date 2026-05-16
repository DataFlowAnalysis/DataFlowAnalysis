# 📊 Model: (AccessControl-DAC-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor (No Violation)" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-DAC-no-violation/DAC-no-violation.json"></VPButton>
<VPButton text="Open In Online Editor (Isolation Violation)" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-DAC-violation/DAC-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://openlibrary.org/books/OL17011110M/Securing_information_and_communications_systems)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-DAC-violation)

## 📝 Short Description
This case is about a file system for pictures in a family.

## 🔤 Abbreviations
None.

## 📖 Extensive Description
There are three family users that use a store __Family Pictures__. The __Mother__ and __Dad__ can *add_pictures* and *read_pictures*. The __Aunt__ can only *read_pictures*. An __Indexing Bot__ might discover the file sharing system but must not access it.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **TraversedNodes**: Each node visited by a certain data flow adds its own tag. This can be `addPicture`, `pictureStorage`, `readPicture`, `mother`, `dad`, `aunt` and `indexingBot`.
### 🏷️ Node Labels:
- **Identity**: This node describes a certain user. There are `Mother`, `Dad`, `Aunt` and `IndexingBot`.
- **Owner**: `Mother` is the owner of the data store.
- **Read**: Designates the right to read from the data store. `Mother`, `Dad`, `Aunt` may read from it.

## ⚠️ Constraints
### Isolation
The __Indexing Bot__ is not allowed to read or access __Family Pictures__ in any way.
- `Isolation: data !Read.IndexingBot neverFlows vertex Identity.IndexingBot`

## 🚨 Violations
Although no violations were found in the original architecture, we have slightly modified the diagram to produce one alternate version in which violations are introduced:

- __IndexingBot__ reads *pictures*. 


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

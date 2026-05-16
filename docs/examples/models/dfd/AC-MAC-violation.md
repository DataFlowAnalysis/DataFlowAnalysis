# 📊 Model: (AccessControl-MAC-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor (No Violation)" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-MilitaryAircraftController-no-violation/MAC-no-violation.json"></VPButton>
<VPButton text="Open In Online Editor (SC Violation)" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-MilitaryAircraftController-violation/MAC-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://openlibrary.org/books/OL17011110M/Securing_information_and_communications_systems)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-MilitaryAircraftController-violation)

## 📝 Short Description
The case is about an airspace monitoring system for civil and military planes.

## 🔤 Abbreviations
None.

## 📖 Extensive Description
There are three types of users. A __Clerk__ creates and stores weather reports. A __Flight Controller__ registers civil airplanes, finds their positions and determines new routes based on other plane positions and the weather reports of the clerk. The __Military Flight Controller__ performs the same tasks for military airplanes and also considers the positions of civil airplanes. 
__Weather Data__ is provided by a __Clerk__ and, as it is `Unclassified` Data, supplied a *weather report* to other processes.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **ClassificationLevel**: There are three classification levels for a data flow. These are the same as the clearance levels for nodes. A data flow may not traverse a node it for which it does not have the needed classification level.
### 🏷️ Node Labels:
- **ClearanceLevel**: There are three clearance levels. These are, in descending hierarchy: `Secret`, `Classified`, `Unclassified`. Each following level is a subset of the preceding level(s).

## ⚠️ Constraints
### SecretConstraint
This constraint ensures that data designated `Secret` does not flow to nodes with clearance level `Classified`:
- `SecretConstraint: data ClassificationLevel.Secret neverFlows vertex ClearanceLevel.Classified`

### ClassifiedConstraint
This constraint ensures that data designated `Classified` does not flow to nodes with clearance level `Unclassified`:
- `ClassifiedConstraint:  ClassificationLevel.Classified neverFlows vertex ClearanceLevel.Unclassified`

## 🚨 Violations
Although no violations were found in the original architecture, we have slightly modified the diagram to produce one alternate version in which violations are introduced:

- The flight controller uses the positions of military planes to determine a new route violating SecretConstraint. This additional flow is called __plane_positions__.

<script setup>
import { VPButton } from 'vitepress/theme'
</script>

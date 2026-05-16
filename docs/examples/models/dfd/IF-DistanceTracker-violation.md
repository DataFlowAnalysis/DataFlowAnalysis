# 📊 Model: (InformationFlow-DistanceTracker-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor (No Violation)" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-DistanceTracker-no-violation/DistanceTracker-no-violation.json"></VPButton>
<VPButton text="Open In Online Editor (DC Violation)" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-DistanceTracker-violation/DistanceTracker-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-DistanceTracker-violation)

## 📝 Short Description
The case is about a user that wants to track running statistics by an external service.

## 🔤 Abbreviations
None.

## 📖 Extensive Description
The __User__ sends his/her *location* to a *Distance Tracker* service that stores locations in the __Location Store__ and derives the run distance. This happens with the *consent* of the __User__ Afterwards, this service transmits the *distance* to a __Tracking Service__.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **ClassificationLevel**: These are classified as either `User`, `UserTrackingService`, and `OnlyDistance`, depending on which area the data flow is allowed to flow through.
### 🏷️ Node Labels:
- **ClearanceLevel**: These are the same as the data labels. `User`, `UserTrackingService`, and `OnlyDistance` denote which **ClassificationLevel** is allowed to flow through a given node.

## ⚠️ Constraint
### DistanceConstraint
This constraint ensures that no data flow passes a node it does not have the corresponding **ClassificationLevel** for.
- `DistanceConstraint: ClassificationLevel.UserTrackingService neverFlows vertex ClearanceLevel.OnlyDistance`

## 🚨 Violations
Although no violations were found in the original architecture, we have slightly modified the diagram to produce one alternate version in which violations are introduced:

- Calculated distance can bypass the declassification process with *distance_violation*, which implies a higher classification level.


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

# 📊 Model: (AccessControl-DistanceTracker-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-DistanceTracker-violation/DistanceTracker-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-DistanceTracker-violation)

## 📝 Short Description
The case is about a user that wants to track running statistics by an external service.

## 🔤 Abbreviations
None.

## 📖 Extensive Description
The __User__ sends his/her *location* to a *Distance Tracker* service that stores locations in the __Location Store__ and derives the run distance. This happens with the *consent* of the __User__. Afterwards, this service transmits the *distance* to a __Tracking Service__. 

## 🏷️ Label Description
### 🗂️ Data Labels:
- **AccessRights**: These are classified as either `User`, `TrackingService`, and `DistanceTracker`, depending on which area the data flow is allowed to flow through.
### 🏷️ Node Labels:
- **Roles**: These are the same as the data labels. `User`, `TrackingService` and `DistanceTracker` are the three parts of the model. Each node is part of exactly one of these.

## ⚠️ Constraint
### DistanceTrackerSecurity
This constraint ensures that no data flow passes a node it does not have the corresponding label (and access right) for.
- `DistanceTrackerSecurity: !AccessRights.DistanceTracker neverFlows vertex Roles.DistanceTracker`

## 🚨 Violations
The error introduced in the case is that the calculated distance can bypass the declassification process (__confirm_distance__), which implies a higher classification level. 


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

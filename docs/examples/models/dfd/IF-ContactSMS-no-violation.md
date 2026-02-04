# 📊 Model: (InformationFlow-ContactSMS-no-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-ContactSMS-no-violation/ContactSMS-no-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-ContactSMS-no-violation)

## 📝 Short Description
The case is about a user managing contacts and sending a SMS.

## 🔤 Abbreviations
- `SMS`: Short Message Service

## 📖 Extensive Description
A __User__ can manage their contacts in the __Contact Store__. When sending an SMS, they choose a contact by *criteria*, for which __Extract Number__ adds the `Receiver` Data Label. __Send SMS__ combines the extracted number with the message and forwards these to the __SMS Gateway__.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **ClassificationLevel**: This label designates the node types a data flow may visit. There are `User` and `UserReceiver` types.
### 🏷️ Node Labels:
- **ClearanceLevel**: This label categorizes nodes into `User` and `UserReceiver` types, denoting which access rights a data flow must have to visit a node.

## ⚠️ Constraints
### ContactDirect
Data may only flow into `ClearanceLevel` nodes if the flow has the corresponding `ClassificationLevel` label.
- `ContactDirect: data !ClassificationLevel.UserReceiver neverFlows vertex ClearanceLevel.UserReceiver`

## 🚨 Violations
None.

<script setup>
import { VPButton } from 'vitepress/theme'
</script>

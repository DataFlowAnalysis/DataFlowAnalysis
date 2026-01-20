# 📊 Model: (AccessControl-ContactSMS-no-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-ContactSMS-no-violation/ContactSMS-no-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/AC-ContactSMS-no-violation)

## 📝 Short Description
The case is about a user managing contacts and sending a SMS.

## 🔤 Abbreviations
- `SMS`: Short Message Service

## 📖 Extensive Description
A __User__ can manage their contacts in the __Contact Store__. When sending an SMS, they choose a contact by *criteria*, for which __Extract Number__ adds the `Receiver` Data Label. __Send SMS__ combines the extracted number with the message and forwards these to the __SMS Gateway__.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **AccessRights**: This label designates the node types a data flow may visit. There are `User` and `Receiver` types.
### 🏷️ Node Labels:
- **Role**: This label categorizes nodes into `User` and `Receiver` types, depending on which part of the system they belong to.

## ⚠️ Constraints
### AccessRights
Data may only flow into `Role` nodes if the flow has the corresponding `AccessRights` label.
- `AccessRights: data !AccessRights.Receiver neverFlows vertex Role.Receiver`

## 🚨 Violations
None.


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

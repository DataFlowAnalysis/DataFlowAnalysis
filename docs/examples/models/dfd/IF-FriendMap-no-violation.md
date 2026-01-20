# 📊 Model: (InformationFlow-FriendMap-no-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-FriendMap-no-violation/FriendMap-no-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703905)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-FriendMap-no-violation)

## 📝 Short Description
The case is about visualizing the locations of friends in a map and posting it on a social network.

## 🔤 Abbreviations
- `SNAPP`: Social Network App
- `Local DS`: Local DataStore

## 📖 Extensive Description
The user `Alice` reads friend locations (*location_bob* and *location_alice*) from a __SNAPP__ and stores them in a __Local DS__. Afterwards, the system loads *map_code* from a __Map Provider__ and the __Friend Map__ app. The code uses `Google` to create the map. Alice posts this map on the `SocialNetwork`.
The majority of the components are located in a `Trust` **Zone**. When creating a map, data flows through `Google`, which is located in the `Attack` **Zone**. This is why data is encrypted and decrypted around `Google`.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **Level**: There are two types of levels: `High` and `Low`. `High` dominates `Low`.
### 🏷️ Node Labels:
- **Actor**: There are four actors in this model: `Google`, `Alice`, `SocialNetwork` and `CreateMap`. A node is not nessecarily part of a zone.
- **Zone**: There are two zones in this model: `Attack` and `Trust`. A node always part of one of these zones.

## ⚠️ Constraints
### Safety
System parts or actors in the attack zone must not have access to data classified `High`:
- `Safety: data Level.High neverFlows vertex Zone.Attack`

## 🚨 Violations
None.


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

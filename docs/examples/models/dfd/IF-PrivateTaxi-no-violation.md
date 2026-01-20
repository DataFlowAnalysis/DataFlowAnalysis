# 📊 Model: (InformationFlow-PrivateTaxi-no-violation)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-PrivateTaxi-no-violation/PrivateTaxi-no-violation.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
[View Source](https://opus.bibliothek.uni-augsburg.de/opus4/frontdoor/index/index/docId/4339)
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/IF-PrivateTaxi-no-violation)

## 📝 Short Description
The case is about a private taxi service that connects riders and drivers.

## 🔤 Abbreviations
- `CalcDistanceService`: Service for calculating distance.

## 📖 Extensive Description
__Riders__ and __Drivers__ have user accounts at the `PrivateTaxiService`. __Riders__ and __Drivers__ calculate routes to their __destinations__ based on their __locations__. They __encrypt__ their route for a `CalcDistanceService` and send it to the `PrivateTaxiService`. The `PrivateTaxiService` uses the `CalcDistanceService` to determine the proximity of routes (__calc_proximity__). The __Rider__ selects one of the matches (__find_match__ and __select_match__) and encrypts the route for the __Driver__. __Drivers__ decrypt the route (__decrypt_route__) to navigate to the __Rider__ and bring him/her to the destination. 

## 🏷️ Label Description
### 🗂️ Data Labels:
- **PublicKeyOf**: This characteristic type describes that the data flow contains a public key of an **Entity**.
- **PrivateKeyOf**: This characteristic type describes that the data flow contains a private key of an **Entity**.
- **DecryptableBy**: This characteristic type describes that a data item can be decrypted with any of the private keys of the specified entities. 
- **CriticalDataType**: This characteristic type marks whether a flow contains the critical data type `RouteDataType` or `ContactInformation`.
- **EncryptedContent**: This characteristic type marks that an encrypted flow contains either the critical data `RouteDataType` or `ContactInformation`.
### 🏷️ Node Labels:
- **Entity**: This designates a node to be part of an **Entity**. These are: `Driver`, `Rider`, `CalcDistanceService` or `PrivateTaxiService`.

## ⚠️ Constraints
### NeverKnowRoutes
The Private Taxi Service must never get to know the routes of drivers or riders:
- `NeverKnowRoutes: CriticalDataType.RouteDataType neverFlows vertex Entity.PrivateTaxi`

### NeverKnowContactInfo 
The Distance Calculation Service must never get to know contact information of drivers or riders:
- `NeverKnowContactInfo: CriticalDataType.ContactInformation neverFlows vertex Entity.CalcDistanceService`

## 🚨 Violations
None.


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

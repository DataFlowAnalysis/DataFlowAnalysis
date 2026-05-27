# 📊 Diagram: EVerest
::: tip Available Online
This model (converted to a data flow diagram) is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/pcm/EVerest/everest.json&layout=true"></VPButton>
:::

## 🔗 Link to Original Paper/Article
<!--[View Full Main Source](link)-->

[Open Example Model in Example Models Bundles](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/pcm/EVerest)

## 📝 Short Description
The EVerest model is based on an industry-driven open source project for Electric Vehicle charging stations and contains security requirements, documentation, a software architecture, and code. The requirements are manually labeled and include labels for security objectives, security elements, and trace links.

## 🔤 Abbreviations 
- EV: Electric Vehicle 
- TPM: Trusted platform module

## 📖 Extensive Description (if possible)
## 🏷️ Label description

- ### 🗂️ Data Labels:
    - ### Status 
        - __Encrypted__: Data is encrypted
        - __Hashed__: Data is hashed
        - __Sensitive__: Data contains sensitive information
        - __Anonymized__: Data is fully anonymized 
        - __Signed__: Data is signed
        - __Token__: Data is a token
        - __Certificate__: Data is a certificate used for signing
        - __PaymentInformation__: Data is payment information 
        - __Firmware__: Data is firmware
- ### 🏷️ Node Labels:
    - ### Actor 
        - __EndUser__: Actions performed by the end user 
        - __Engineer__: Actions performed by system engineers 
    - ### ActorStatus
        - __Authorized__: Actor is authorized to access the component
    - ### Component 
        - __API__: Actions belonging to the API component
        - __EVSESecurity__: Actions performed by the security component of the system 
        - __PN532TokenProvider__: Actions performed by the token provider
        - __Logs__: Actions concerning logging
    - ### Location
        - __ChargingStation__: Actions performed on Charging station 
        - __TPM__: Actions performed on the trusted platform module
        - __UpdateServer__: Actions performed by the update server
        - __PaymentProvider__: Actions performed by the payment provider
        - __Car__: Actions performed on the car
        - __ChargingStationManagementSystem__: Actions performed by the system responsible for the charging stations
        - __External__: Actions performed by external entities 
        - __LocalStorage__: Actions in local storage

## ⚠️ Constraints
[see expected results](https://github.com/DataFlowAnalysis/DataFlowAnalysis/blob/main/bundles/org.dataflowanalysis.examplemodels/src/org/dataflowanalysis/examplemodels/results/pcm/scenarios/EVerest.java)

## 🚨 Violations
[see expected results](https://github.com/DataFlowAnalysis/DataFlowAnalysis/blob/main/bundles/org.dataflowanalysis.examplemodels/src/org/dataflowanalysis/examplemodels/results/pcm/scenarios/EVerest.java)


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

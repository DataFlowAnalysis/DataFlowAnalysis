# 📊 Diagram: (Patient Monitoring System Overview)

::: tip Available Online
This model is available to view using the online editor!
<VPButton text="Open In Online Editor" href="https://editor.dataflowanalysis.org/?file=https://raw.githubusercontent.com/DataFlowAnalysis/DataFlowAnalysis/refs/heads/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/
PatientMonitoringSystemPaper/Overview.json"></VPButton>
::: 

## 🔗 Link to Original Paper/Article
<!--[View Full Main Source](link)-->
[Open Example Model in Example Models Bundle](https://github.com/DataFlowAnalysis/DataFlowAnalysis/tree/main/bundles/org.dataflowanalysis.examplemodels/scenarios/dfd/PatientMonitoringSystemPaper)

## 📝 Short Description
This Diagram shows the structure of the Patient Ponitoring System. A wearable Sensor periodically sends data to a data sync and to a local risk assessment unit. A General Practitioner is able to see this data and perform remote analysis on the patient's health. In extreme cases, Emergency Services are alerted.

## 🔤 Abbreviations
- `GP`: General Practitioner
- `his`: Hospital Information System
- `PMS`: Patient Monitoring System
- `ML`: Machine Learning

## 📖 Extensive Description
The patient is monitored via a __Wearable Sensor__, which *sends_data* periodically to either be *stored* or *evaluated* locally.
A __General Practitioner__ can enter a __GP Portal__ to manually assess the __Patient Data__ in the `BackEnd` of the PMS. They are aided by a __ML Model Provider__ which is part of the __Clinical Risk Assessment__ for the patient. 
The __Risk Level__ for the patient is updated regularly and in the event of an emergency, both the local and clinical risk assessment parts can *alert* __Emergency Services__.

## 🏷️ Label description
### 🗂️ Data Labels:
None.
### 🏷️ Node Labels:
- **PMS**: The PMS is comprised of two parts: A `SmartphoneAppGateway` and a `BackEnd` part. These parts communicate via a __Data Sync__. 

## ⚠️ Constraints
None.

## 🚨 Violations
None.


<script setup>
import { VPButton } from 'vitepress/theme'
</script>

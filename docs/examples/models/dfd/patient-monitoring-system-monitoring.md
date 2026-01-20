# 📊 Diagram: (Patient Monitoring System Monitoring)

## 🔗 Link to Original Paper/Article
<!--[View Full Main Source](link)-->

## 📝 Short Description

This Diagram shows the structure of the Patient Ponitoring System with a focus on the monitoring features of the system.

## 🔤 Abbreviations

- `GP`: General Practitioner
- `PMS`: Patient Monitoring System
- `GPS`: Global Positioning System

## 📖 Extensive Description

A __Wearable Sensor__ sends different types of biological data to a __Local Buffer__. This data is aggregated and the *aggregated_data* is sent to (abstracted) __Communication Technologies__ and made available to a __GP Portal__ via the __PMS Back-End__. The __Physician__ can change the rate of transmission for the __Wearable Sensor__, __GPS Chip__ and __Smart Scale__ as well as *change_risk_level* for the patient's health.
The patient can see a simplified version of their health data via a __Patient Interface__ via their local App.

## 🏷️ Label description
### 🗂️ Data Labels:
- **DataType**: There are different types of sata in this system: `RawData` from the different sensors, `PackagedData` in communication channels` and `SimplifiedData`, which is the only data type the patient may access.

### 🏷️ Node Labels:
- **PMS**: The PMS is comprised of two parts: A `Gateway` and a `BackEnd` part. These parts communicate via __Communication Technologies__. 
- **Role**: There are three other actors in this system: `AbstractCommunication` technologies which connect the technical parts of the PMS, the  `Physician` and the `Patient`.

## ⚠️ Constraints
### DataSec
This constraint ensures that no raw data from the sensors flows to the __Physician__ directly and only via `AbstractCommunication`, which uses `PackagedData` formats.
- `DataSec: data DataType.RawData neverFlows vertex PMS.BackEnd`

### SimplePatient
The __Patient__ may only see a part of the data and risk analysis there is. This data is labelley `SimplifiedData`. The constraint ensures that this is the only data the patient may access.
- `SimplePatient: data DataType.PackagedData neverFlows vertex Role.Patient`

## 🚨 Violations

None.

# 📊 Diagram: (Smart Speaker Platform from `Precise Analysis of Purpose Limitation in Data Flow Diagrams)

## 🔗 Link to Original Paper
<!--[View Full Main Source](https://doi.org/10.1145/3538969.3539010)-->

## 🔗 Link to GitHub Repository
<!--[Repository](https://github.com/alshareef-hanaa/PL-DFD)-->

## 📝 Short Description

This diagram shows a fictional Smart Speaker system. The use cases include the download and install process aswell as voice-guided command sequences and data exchanges with third parties.

## 🔤 Abbreviations

- `CMD`: Command

## 📖 Extensive Description
The __Device Owner__ can first download the mobile *app*
for using the smart __Speaker__. After the initial setup, the user will login
to the app and connect the __Speaker__ to the local network (the __Router__).
The user can also log in to the __Music Store__. The user activates the
speakers’ microphone and sends *voice_requests* which are processes
by the __Provider__, returning the corresponding *command* back to the
__Speaker__. The __Speaker__ then invokes the Music Store API provided
capabilities to stream the desired content. Finally, the __Provider__ may
send certain aggregated statistics about their history of clienteles’
requests to __Third-party Partners__.

## 🏷️ Label description

- ### 🗂️ Data Labels:
    - **Purpose**: These labels mark the intended purpose of a given data flow between two nodes. These can be: `Install`, `Login`, `Authenticate`, `Register`, `Streaming`, `NotifyUser`, `CMDProcessing`, `VoiceProcessing`, `TriggerMusicStore`, `Marketing`, `Storage`, `AIProcessing` and `NoPermission`.
- ### 🏷️ Node Labels:
    - **AllowedPurpose**: These labels designate allowed purposes to nodes. They are a subset of **Purpose** labels: `NotifyUser`, `Streaming`, `Register`, `Marketing` and `Storage`.
    - **ComponentCategory**: This labels the nodes according to their place in the overall system. There are: `UserHomeDevice`, `MusicStore`, `UserPhone`, `Provider`, `DeviceOwner`, `Router`, `ThirdPartyPartner` and `AppStore`.

## ⚠️ Constraints
### StorageConstraint
This constraint ensures that data flows marked for `Storage` **Purposes** are never handed to an entity from the **ComponentCategory** `UserHomeDevice`.

- `StorageConstraint: data Purpose.Storage neverFlows vertex ComponentCategory.ThirdPartyPartner`

### PermissionConstraint
This constraint ensures that data without permissions is not processed withhin the `UserHomeDevice`.

- `PermissionConstraint: data Purpose.NoPermission neverFlows vertex ComponentCategory.UserHomeDevice`

## 🚨 Violations

User data marked for storage flows to a __Third-Party Partner__, which violates the StorageConstraint.

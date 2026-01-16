# 📊 Model: (AccessControl-ContactSMS-no-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)

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

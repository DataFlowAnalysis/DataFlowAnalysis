# 📊 Model: (AccessControl-ContactSMS-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)

## 📝 Short Description
The case is about a user managing contacts and sending a SMS. The user can add, remove and list contacts. To send a SMS, the user selects a contact and writes a message. The system extracts the number and sends the number and message to a SMS gateway. 

## 🔤 Abbreviations
- `SMS`: Short Message Service

## 📖 Extensive Description
A `User` can manage their contacts in the `Contact Store`. When sending an SMS, they choose a contact by *criteria*, for which `extract number` adds the `Receiver` Data Label. `send SMS` combines the extracted number with the message and forwards these to the `SMS Gateway`.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **AccessRights**: This label designates the node types a data flow may visit. There are `User` and `Receiver` types.
### 🏷️ Node Labels:
- **Role**: This label categorizes nodes into `User` and `Receiver` types, depending on which part of the system they belong to.

## ⚠️ Constraints
### Constraint Title
Data may only flow into `Role` nodes if the flow has the corresponding `AccessRights` label.
- `data !AccessRights.Receiver neverFlows vertex Role.Receiver`

## 🚨 Violations
The introduced flow *contact_direct* bypasses __extract_number__, which implies a missing access permission.

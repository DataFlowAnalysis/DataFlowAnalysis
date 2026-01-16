# 📊 Model: (InformationFlow-ContactSMS-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703910)

## 📝 Short Description
The case is about a user managing contacts and sending a SMS.

## 🔤 Abbreviations
- `SMS`: Short Message Service

## 📖 Extensive Description
A __User__ can manage their contacts in the __Contact Store__. When sending an SMS, they choose a contact by *criteria*, for which __Extract Number__ adds the `UserReceiver` Data Label. __Send SMS__ combines the extracted number with the message and forwards these to the __SMS Gateway__.

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

The introduced flow *contact_direct* bypasses __Extract Number__, which implies a missing access permission.

# 📊 Model: (InformationFlow-Hospital-no-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703905)

## 📝 Short Description

The case is about an employee of a hospital that loads and updates a patient list via a hospital app. The hospital app authorizes the employee and requests the patient list from a database. The employee reads and modifies the list. The modified list is written back to the database.

## 🔤 Abbreviations

- **Hospital DS**: Hospital DataStore

## 📖 Extensive Description
(In this discription you may use/are encouraged to use __Vertex Names__ and *Variable/Edge Names* so that the reader may follow the flow)


## 🏷️ Label Description
### 🗂️ Data Labels:
- **Level**: This label denotes the zones a data flow may access. There are levels `High` and `Low`.
- **LevelBeforeEncryption**: This label stores the security classification of a data flow before it is encrypted with the `declassify` node. This can be `High` or `Low`.
### 🏷️ Node Labels:
- **Zone**: There are two `Zones`: `Trust` and `Attack`. A node can be in either or none of these `Zones`.

## ⚠️ Constraint
### SafetyConstraint
The fundamental requirement is that system parts or actors in the attack zone must not have access to data classified High:
- `data Level.High neverFlows vertex Zone.Attack`

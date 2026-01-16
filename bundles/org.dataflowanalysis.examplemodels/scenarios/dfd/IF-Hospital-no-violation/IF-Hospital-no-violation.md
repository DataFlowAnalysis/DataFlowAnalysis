# 📊 Model: (InformationFlow-Hospital-no-violation)

## 🔗 Link to Original Paper/Article
[View Source](https://ieeexplore.ieee.org/document/8703905)

## 📝 Short Description

The case is about an employee of a hospital that loads and updates a patient list via a hospital app. The hospital app authorizes the employee and requests the patient list from a database. The employee reads and modifies the list. The modified list is written back to the database.

## 🔤 Abbreviations

- **Hospital DS**: Hospital DataStore

## 📖 Extensive Description

The __Hospital App__ provides the means to modify a patient list. __Receive Patient List__ combines the *request* and *patient_list_encrypted* flows and forwards a *patient_list* to the Employee. The __Employee__ can __Modify Patient List__ and write the *modified_list* back to the __Hospital DS__.
Because the *patient_list* from the __Hospital DS__ is encrypted, the __Attacker__ cannot gain sensitive data.

## 🏷️ Label Description
### 🗂️ Data Labels:
- **Level**: This label denotes the zones a data flow may access. There are levels `High` and `Low`.
- **LevelBeforeEncryption**: This label stores the security classification of a data flow before it is encrypted with the `declassify` node. This can be `High` or `Low`.
### 🏷️ Node Labels:
- **Zone**: There are two `Zones`: `Trust` and `Attack`. A node can be in either or none of these `Zones`.

## ⚠️ Constraint
### SafetyConstraint
The fundamental requirement is that system parts or actors in the attack zone must not have access to data classified High:
- `SafetyConstraint: data Level.High neverFlows vertex Zone.Attack`

## 🚨 Violations

None.
